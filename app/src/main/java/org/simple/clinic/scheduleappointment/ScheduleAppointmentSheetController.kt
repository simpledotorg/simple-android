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
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
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

  private val latestAppointmentDateScheduledSubject = BehaviorSubject.create<LocalDate>()

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    val configuredAppointmentDatesStream = config
        .map { it.possibleAppointments }
        .map { possibleAppointments -> possibleAppointments.map(this::localDateFromScheduleAppointment) }
        .share()

    return Observable.mergeArray(
        incrementDecrements(replayedEvents, configuredAppointmentDatesStream),
        enableIncrements(configuredAppointmentDatesStream),
        enableDecrements(configuredAppointmentDatesStream),
        showManualAppointmentDateSelector(replayedEvents),
        scheduleAutomaticAppointmentForDefaulters(replayedEvents),
        scheduleCreates(replayedEvents)
    )
  }

  private fun incrementDecrements(
      events: Observable<UiEvent>,
      configuredAppointmentDatesStream: Observable<List<LocalDate>>
  ): Observable<UiChange> {
    val selectDefaultAppointmentOnSheetCreated = events
        .ofType<ScheduleAppointmentSheetCreated>()
        .withLatestFrom(config) { _, config -> config.defaultAppointment }
        .map(this::localDateFromScheduleAppointment)

    val selectNextAppointmentDate = events
        .ofType<AppointmentDateIncremented>()
        .withLatestFrom(latestAppointmentDateScheduledSubject, configuredAppointmentDatesStream)
        { _, lastScheduledAppointmentDate, configuredAppointmentDates ->
          nextConfiguredAppointmentDate(lastScheduledAppointmentDate, configuredAppointmentDates)
        }

    val selectPreviousAppointmentDate = events
        .ofType<AppointmentDateDecremented>()
        .withLatestFrom(latestAppointmentDateScheduledSubject, configuredAppointmentDatesStream)
        { _, lastScheduledAppointmentDate, configuredAppointmentDates ->
          previousConfiguredAppointmentDate(lastScheduledAppointmentDate, configuredAppointmentDates)
        }

    val selectCalendarDate = events
        .ofType<AppointmentCalendarDateSelected>()
        .map { it.selectedDate }

    return Observable
        .merge(
            selectDefaultAppointmentOnSheetCreated,
            selectNextAppointmentDate,
            selectPreviousAppointmentDate,
            selectCalendarDate
        )
        .distinctUntilChanged()
        .doOnNext(latestAppointmentDateScheduledSubject::onNext)
        .map { appointmentDate -> { ui: Ui -> ui.updateScheduledAppointment(appointmentDate) } }
  }

  private fun enableIncrements(configuredAppointmentDateStream: Observable<List<LocalDate>>): Observable<UiChange> {
    return latestAppointmentDateScheduledSubject
        .withLatestFrom(configuredAppointmentDateStream) { latestAppointmentScheduledDate, configuredAppointmentDates ->
          latestAppointmentScheduledDate < configuredAppointmentDates.last()
        }
        .distinctUntilChanged()
        .map { enable -> { ui: Ui -> ui.enableIncrementButton(enable) } }
  }

  private fun enableDecrements(configuredAppointmentDateStream: Observable<List<LocalDate>>): Observable<UiChange> {
    return latestAppointmentDateScheduledSubject
        .withLatestFrom(configuredAppointmentDateStream) { latestAppointmentScheduledDate, configuredAppointmentDates ->
          latestAppointmentScheduledDate > configuredAppointmentDates.first()
        }
        .distinctUntilChanged()
        .map { enable -> { ui: Ui -> ui.enableDecrementButton(enable) } }
  }

  private fun showManualAppointmentDateSelector(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ManuallySelectAppointmentDateClicked>()
        .withLatestFrom(latestAppointmentDateScheduledSubject) { _, latestAppointmentScheduledDate ->
          { ui: Ui -> ui.showManualDateSelector(latestAppointmentScheduledDate) }
        }
  }

  private fun nextConfiguredAppointmentDate(
      latestAppointmentScheduledDate: LocalDate,
      configuredAppointmentScheduledDates: List<LocalDate>
  ): LocalDate {
    return configuredAppointmentScheduledDates
        .find { it > latestAppointmentScheduledDate }
        ?: throw RuntimeException("Cannot find configured appointment date after $latestAppointmentScheduledDate")
  }

  private fun previousConfiguredAppointmentDate(
      latestAppointmentScheduledDate: LocalDate,
      configuredAppointmentScheduledDates: List<LocalDate>
  ): LocalDate {
    return configuredAppointmentScheduledDates
        .findLast { it < latestAppointmentScheduledDate }
        ?: throw RuntimeException("Cannot find configured appointment date before $latestAppointmentScheduledDate")
  }

  private fun scheduleAutomaticAppointmentForDefaulters(events: Observable<UiEvent>): Observable<UiChange> {
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
          scheduleAppointmentForPatient(
              uuid = patientUuid,
              date = LocalDate.now(utcClock).plus(config.appointmentDuePeriodForDefaulters),
              currentFacility = currentFacility,
              appointmentType = Appointment.AppointmentType.Automatic
          )
        }
        .map { Ui::closeSheet }

    val closeSheetWithoutSavingAppointment = isPatientDefaulterStream
        .filter { isPatientDefaulter -> isPatientDefaulter.not() }
        .map { Ui::closeSheet }

    return Observable.merge(saveAppointmentAndCloseSheet, closeSheetWithoutSavingAppointment)
  }

  private fun scheduleCreates(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppointmentDone>()
        .withLatestFrom(
            latestAppointmentDateScheduledSubject,
            patientUuid(events),
            currentFacilityStream()
        ) { _, lastScheduledAppointmentDate, uuid, currentFacility ->
          Triple(lastScheduledAppointmentDate, uuid, currentFacility)
        }
        .flatMapSingle { (date, uuid, currentFacility) -> scheduleAppointmentForPatient(uuid, date, currentFacility, Manual) }
        .map { Ui::closeSheet }
  }

  private fun scheduleAppointmentForPatient(
      uuid: UUID,
      date: LocalDate,
      currentFacility: Facility,
      appointmentType: Appointment.AppointmentType
  ): Single<Appointment> {
    return appointmentRepository
        .schedule(
            patientUuid = uuid,
            appointmentDate = date,
            appointmentType = appointmentType,
            currentFacility = currentFacility
        )
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

  private fun localDateFromScheduleAppointment(appointment: ScheduleAppointment): LocalDate {
    return LocalDate.now(utcClock).plus(appointment.timeAmount.toLong(), appointment.chronoUnit)
  }
}
