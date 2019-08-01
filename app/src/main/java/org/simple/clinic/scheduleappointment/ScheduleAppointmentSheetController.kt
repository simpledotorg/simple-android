package org.simple.clinic.scheduleappointment

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.Appointment.AppointmentType.Automatic
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit.DAYS
import javax.inject.Inject

typealias Ui = ScheduleAppointmentSheet
typealias UiChange = (Ui) -> Unit

class ScheduleAppointmentSheetController @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val configProvider: Single<AppointmentConfig>,
    private val utcClock: UtcClock,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        incrementDecrements(replayedEvents),
        enableIncrements(replayedEvents),
        enableDecrements(replayedEvents),
        showCalendar(replayedEvents),
        schedulingSkips(replayedEvents),
        scheduleCreates(replayedEvents)
    )
  }

  private fun incrementDecrements(events: Observable<UiEvent>): Observable<UiChange> =
      latestAppointment(events)
          .distinctUntilChanged()
          .map { appointment -> { ui: Ui -> ui.updateScheduledAppointment(appointment) } }

  private fun enableIncrements(events: Observable<UiEvent>): Observable<UiChange> =
      latestAppointment(events)
          .withLatestFrom(possibleAppointments(events)) { latestAppointment, possibleAppointments ->
            toLocalDate(latestAppointment) < toLocalDate(possibleAppointments.last())
          }
          .distinctUntilChanged()
          .map { enable -> { ui: Ui -> ui.enableIncrementButton(enable) } }

  private fun enableDecrements(events: Observable<UiEvent>): Observable<UiChange> =
      latestAppointment(events)
          .withLatestFrom(possibleAppointments(events)) { latestAppointment, possibleAppointments ->
            toLocalDate(latestAppointment) > toLocalDate(possibleAppointments.first())
          }
          .distinctUntilChanged()
          .map { enable -> { ui: Ui -> ui.enableDecrementButton(enable) } }

  private fun showCalendar(events: Observable<UiEvent>): Observable<UiChange> =
      events
          .ofType<AppointmentChooseCalendarClicks>()
          .withLatestFrom(latestAppointment(events)) { _, appointment ->
            { ui: Ui -> ui.showCalendar(toLocalDate(appointment)) }
          }

  private fun latestAppointment(events: Observable<UiEvent>): Observable<ScheduleAppointment> =
      latestDateAction(events)
          .withLatestFrom(possibleAppointments(events))
          .scan(ScheduleAppointment.DEFAULT) { latestAppointment, (action, possibleAppointments) ->
            newAppointment(action, latestAppointment, possibleAppointments)
          }
          .skip(1)

  private fun newAppointment(
      action: UiEvent,
      latestAppointment: ScheduleAppointment,
      possibleAppointments: List<ScheduleAppointment>
  ): ScheduleAppointment =
      when (action) {
        AppointmentDateIncremented -> nextAppointment(latestAppointment, possibleAppointments)
        AppointmentDateDecremented -> previousAppointment(latestAppointment, possibleAppointments)
        is ScheduleAppointmentSheetCreated -> action.defaultAppointment
        is AppointmentCalendarDateSelected -> toScheduleAppointment(action)
        else -> ScheduleAppointment.DEFAULT
      }

  private fun toScheduleAppointment(dateSelected: AppointmentCalendarDateSelected): ScheduleAppointment {
    val days = DAYS.between(LocalDate.now(utcClock), LocalDate.of(
        dateSelected.year,
        dateSelected.month,
        dateSelected.dayOfMonth
    ))
    return ScheduleAppointment(
        displayText = "$days days",
        timeAmount = days.toInt(),
        chronoUnit = DAYS
    )
  }

  private fun nextAppointment(
      latestAppointment: ScheduleAppointment,
      possibleAppointments: List<ScheduleAppointment>
  ): ScheduleAppointment {
    val latestDate = toLocalDate(latestAppointment)
    possibleAppointments.forEachIndexed { index, appointment ->
      val appointmentLocalDate = toLocalDate(appointment)
      if (appointmentLocalDate == latestDate) return possibleAppointments[index + 1]
      else if (appointmentLocalDate > latestDate) return possibleAppointments[index]
    }
    return ScheduleAppointment.DEFAULT
  }

  private fun previousAppointment(
      latestAppointment: ScheduleAppointment,
      possibleAppointments: List<ScheduleAppointment>
  ): ScheduleAppointment {
    val latestDate = toLocalDate(latestAppointment)
    val reversedPossibleAppointments = possibleAppointments.reversed()
    reversedPossibleAppointments.forEachIndexed { index, appointment ->
      val appointmentLocalDate = toLocalDate(appointment)
      if (appointmentLocalDate == latestDate) return reversedPossibleAppointments[index + 1]
      else if (appointmentLocalDate < latestDate) return reversedPossibleAppointments[index]
    }
    return ScheduleAppointment.DEFAULT
  }

  private fun latestDateAction(events: Observable<UiEvent>) =
      Observable.merge(
          events.ofType<AppointmentDateIncremented>(),
          events.ofType<AppointmentDateDecremented>(),
          events.ofType<ScheduleAppointmentSheetCreated>(),
          events.ofType<AppointmentCalendarDateSelected>()
      )

  private fun possibleAppointments(events: Observable<UiEvent>) =
      events
          .ofType<ScheduleAppointmentSheetCreated>()
          .map { it.possibleAppointments }

  private fun schedulingSkips(events: Observable<UiEvent>): Observable<UiChange> {
    val combinedStreams = Observables.combineLatest(
        events.ofType<SchedulingSkipped>(),
        patientUuid(events),
        configProvider.toObservable()
    )
    val isPatientDefaulterStream = combinedStreams
        .switchMap { (_, patientUuid) -> patientRepository.isPatientDefaulter(patientUuid) }
        .replay()
        .refCount()

    val saveAppointmentAndCloseSheet = isPatientDefaulterStream
        .filter { isPatientDefaulter -> isPatientDefaulter }
        .withLatestFrom(
            patientUuid(events),
            configProvider.toObservable(),
            currentFacilityStream()
        ) { _, patientUuid, config, currentFacilty ->
          Triple(patientUuid, config, currentFacilty)
        }
        .flatMapSingle { (patientUuid, config, currentFacility) ->
          appointmentRepository
              .schedule(
                  patientUuid = patientUuid,
                  appointmentDate = LocalDate.now(utcClock).plus(config.appointmentDuePeriodForDefaulters),
                  appointmentType = Automatic,
                  currentFacility = currentFacility)
              .map { { ui: Ui -> ui.closeSheet() } }
        }

    val closeSheetWithoutSavingAppointment = isPatientDefaulterStream
        .filter { isPatientDefaulter -> isPatientDefaulter.not() }
        .map { { ui: Ui -> ui.closeSheet() } }

    return Observable.merge(saveAppointmentAndCloseSheet, closeSheetWithoutSavingAppointment)
  }

  private fun scheduleCreates(events: Observable<UiEvent>): Observable<UiChange> =
      events
          .ofType<AppointmentDone>()
          .withLatestFrom(
              latestAppointment(events),
              patientUuid(events),
              currentFacilityStream()
          ) { _, latestAppointment, uuid, currentFacility ->
            Triple(toLocalDate(latestAppointment), uuid, currentFacility)
          }
          .flatMapSingle { (date, uuid, currentFacility) ->
            appointmentRepository
                .schedule(
                    patientUuid = uuid,
                    appointmentDate = date,
                    appointmentType = Manual,
                    currentFacility = currentFacility
                )
                .map { { ui: Ui -> ui.closeSheet() } }
          }

  private fun patientUuid(events: Observable<UiEvent>) =
      events
          .ofType<ScheduleAppointmentSheetCreated>()
          .map { it.patientUuid }

  private fun currentFacilityStream(): Observable<Facility> =
      userSession
          .requireLoggedInUser()
          .switchMap(facilityRepository::currentFacility)

  fun toLocalDate(appointment: ScheduleAppointment): LocalDate =
      LocalDate.now(utcClock).plus(appointment.timeAmount.toLong(), appointment.chronoUnit)
}
