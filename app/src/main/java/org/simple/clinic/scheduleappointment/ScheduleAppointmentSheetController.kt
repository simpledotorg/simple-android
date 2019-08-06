package org.simple.clinic.scheduleappointment

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.subjects.BehaviorSubject
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
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.ChronoUnit.DAYS
import java.util.UUID
import javax.inject.Inject

typealias Ui = ScheduleAppointmentSheet
typealias UiChange = (Ui) -> Unit

class ScheduleAppointmentSheetController @Inject constructor(
    private val config: Observable<ScheduleAppointmentConfig>,
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val configProvider: Single<AppointmentConfig>,
    private val utcClock: UtcClock,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository
) : ObservableTransformer<UiEvent, UiChange> {

  private val latestAppointmentSubject = BehaviorSubject.create<ScheduleAppointment>()

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    val possibleAppointmentsStream = config
        .map { it.possibleAppointments }
        .share()

    return Observable.mergeArray(
        incrementDecrements(replayedEvents),
        enableIncrements(possibleAppointmentsStream),
        enableDecrements(possibleAppointmentsStream),
        showCalendar(replayedEvents),
        schedulingSkips(replayedEvents),
        scheduleCreates(replayedEvents)
    )
  }

  private fun incrementDecrements(events: Observable<UiEvent>): Observable<UiChange> {
    val selectDefaultAppointmentOnSheetCreated = events
        .ofType<ScheduleAppointmentSheetCreated>()
        .withLatestFrom(config) { _, config -> config }
        .map { it.defaultAppointment }

    val selectNextAppointmentDate = events
        .ofType<AppointmentDateIncremented>()
        .withLatestFrom(latestAppointmentSubject, config) { _, lastScheduledAppointment, config ->
          nextAppointment(lastScheduledAppointment, config.possibleAppointments)
        }

    val selectPreviousAppointmentDate = events
        .ofType<AppointmentDateDecremented>()
        .withLatestFrom(latestAppointmentSubject, config) { _, lastScheduledAppointment, config ->
          previousAppointment(lastScheduledAppointment, config.possibleAppointments)
        }

    val selectCalendarDate = events
        .ofType<AppointmentCalendarDateSelected>()
        .map(this::toScheduleAppointment)

    return Observable
        .merge(
            selectDefaultAppointmentOnSheetCreated,
            selectNextAppointmentDate,
            selectPreviousAppointmentDate,
            selectCalendarDate
        )
        .distinctUntilChanged()
        .doOnNext { latestAppointmentSubject.onNext(it) }
        .map { appointment -> { ui: Ui -> ui.updateScheduledAppointment(toLocalDate(appointment)) } }
  }

  private fun enableIncrements(possibleAppointmentsStream: Observable<List<ScheduleAppointment>>): Observable<UiChange> {
    return latestAppointmentSubject
        .withLatestFrom(possibleAppointmentsStream) { latestAppointment, possibleAppointments ->
          toLocalDate(latestAppointment) < toLocalDate(possibleAppointments.last())
        }
        .distinctUntilChanged()
        .map { enable -> { ui: Ui -> ui.enableIncrementButton(enable) } }
  }

  private fun enableDecrements(possibleAppointmentsStream: Observable<List<ScheduleAppointment>>): Observable<UiChange> {
    return latestAppointmentSubject
        .withLatestFrom(possibleAppointmentsStream) { latestAppointment, possibleAppointments ->
          toLocalDate(latestAppointment) > toLocalDate(possibleAppointments.first())
        }
        .distinctUntilChanged()
        .map { enable -> { ui: Ui -> ui.enableDecrementButton(enable) } }
  }

  private fun showCalendar(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppointmentChooseCalendarClicks>()
        .withLatestFrom(latestAppointmentSubject) { _, appointment ->
          { ui: Ui -> ui.showCalendar(toLocalDate(appointment)) }
        }
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
    return ScheduleAppointment(displayText = "1 month", timeAmount = 1, chronoUnit = ChronoUnit.MONTHS)
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
    return ScheduleAppointment(displayText = "1 month", timeAmount = 1, chronoUnit = ChronoUnit.MONTHS)
  }

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

  private fun scheduleCreates(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppointmentDone>()
        .withLatestFrom(
            latestAppointmentSubject,
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
  }

  private fun patientUuid(events: Observable<UiEvent>): Observable<UUID> {
    return events
        .ofType<ScheduleAppointmentSheetCreated>()
        .map { it.patientUuid }
  }

  private fun currentFacilityStream(): Observable<Facility> {
    return userSession
        .requireLoggedInUser()
        .switchMap(facilityRepository::currentFacility)
  }

  fun toLocalDate(appointment: ScheduleAppointment): LocalDate {
    return LocalDate.now(utcClock).plus(appointment.timeAmount.toLong(), appointment.chronoUnit)
  }
}
