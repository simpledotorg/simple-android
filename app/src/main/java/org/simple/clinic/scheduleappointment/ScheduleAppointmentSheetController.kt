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
import org.simple.clinic.scheduleappointment.TimeToAppointment.Days
import org.simple.clinic.scheduleappointment.TimeToAppointment.Months
import org.simple.clinic.scheduleappointment.TimeToAppointment.Weeks
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

typealias Ui = ScheduleAppointmentSheet
typealias UiChange = (Ui) -> Unit

class ScheduleAppointmentSheetController @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val configProvider: Observable<AppointmentConfig>,
    private val clock: UserClock,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository
) : ObservableTransformer<UiEvent, UiChange> {

  private val latestAppointmentDateScheduledSubject = BehaviorSubject.create<PotentialAppointmentDate>()

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    val configuredAppointmentDatesStream = generatePotentialAppointmentDatesForScheduling()
        .share()

    return Observable.mergeArray(
        scheduleAppointments(replayedEvents, configuredAppointmentDatesStream),
        enableIncrements(configuredAppointmentDatesStream),
        enableDecrements(configuredAppointmentDatesStream),
        showManualAppointmentDateSelector(replayedEvents),
        scheduleAutomaticAppointmentForDefaulters(replayedEvents),
        scheduleCreates(replayedEvents)
    )
  }

  private fun generatePotentialAppointmentDatesForScheduling(): Observable<List<PotentialAppointmentDate>> {
    return configProvider
        .map { it.scheduleAppointmentsIn }
        .map(this::generatePotentialAppointmentDates)
        .map { appointmentDates -> appointmentDates.distinctBy { it.scheduledFor }.sorted() }
  }

  private fun scheduleAppointments(
      events: Observable<UiEvent>,
      configuredAppointmentDatesStream: Observable<List<PotentialAppointmentDate>>
  ): Observable<UiChange> {

    return Observable
        .merge(
            scheduleDefaultAppointmentDateForSheetCreates(events),
            scheduleOnNextConfiguredAppointmentDate(events, configuredAppointmentDatesStream),
            scheduleOnPreviousConfiguredAppointmentDate(events, configuredAppointmentDatesStream),
            scheduleOnExactDate(events, configuredAppointmentDatesStream)
        )
        .distinctUntilChanged()
        .doOnNext(latestAppointmentDateScheduledSubject::onNext)
        .map { appointmentDate -> { ui: Ui -> ui.updateScheduledAppointment(appointmentDate.scheduledFor, appointmentDate.timeToAppointment) } }
  }

  private fun scheduleOnExactDate(
      events: Observable<UiEvent>,
      configuredAppointmentDatesStream: Observable<List<PotentialAppointmentDate>>
  ): Observable<PotentialAppointmentDate> {
    return events
        .ofType<AppointmentCalendarDateSelected>()
        .map { it.selectedDate }
        .withLatestFrom(configuredAppointmentDatesStream)
        .map { (appointmentDate, potentialAppointmentDates) -> generatePotentialAppointmentDate(appointmentDate, potentialAppointmentDates) }
  }

  private fun scheduleOnPreviousConfiguredAppointmentDate(
      events: Observable<UiEvent>,
      configuredAppointmentDatesStream: Observable<List<PotentialAppointmentDate>>
  ): Observable<PotentialAppointmentDate> {
    return events
        .ofType<AppointmentDateDecremented>()
        .withLatestFrom(latestAppointmentDateScheduledSubject, configuredAppointmentDatesStream)
        { _, lastScheduledAppointmentDate, configuredAppointmentDates ->
          previousConfiguredAppointmentDate(lastScheduledAppointmentDate, configuredAppointmentDates)
        }
  }

  private fun scheduleOnNextConfiguredAppointmentDate(
      events: Observable<UiEvent>,
      configuredAppointmentDatesStream: Observable<List<PotentialAppointmentDate>>
  ): Observable<PotentialAppointmentDate> {
    return events
        .ofType<AppointmentDateIncremented>()
        .withLatestFrom(latestAppointmentDateScheduledSubject, configuredAppointmentDatesStream)
        { _, lastScheduledAppointmentDate, configuredAppointmentDates ->
          nextConfiguredAppointmentDate(lastScheduledAppointmentDate, configuredAppointmentDates)
        }
  }

  private fun scheduleDefaultAppointmentDateForSheetCreates(events: Observable<UiEvent>): Observable<PotentialAppointmentDate> {
    return events
        .ofType<ScheduleAppointmentSheetCreated>()
        .withLatestFrom(configProvider) { _, config -> config.defaultTimeToAppointment }
        .map(::generatePotentialAppointmentDate)
  }

  private fun enableIncrements(configuredAppointmentDateStream: Observable<List<PotentialAppointmentDate>>): Observable<UiChange> {
    return latestAppointmentDateScheduledSubject
        .withLatestFrom(configuredAppointmentDateStream) { latestAppointmentScheduledDate, configuredAppointmentDates ->
          latestAppointmentScheduledDate < configuredAppointmentDates.last()
        }
        .distinctUntilChanged()
        .map { enable -> { ui: Ui -> ui.enableIncrementButton(enable) } }
  }

  private fun enableDecrements(configuredAppointmentDateStream: Observable<List<PotentialAppointmentDate>>): Observable<UiChange> {
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
          { ui: Ui -> ui.showManualDateSelector(latestAppointmentScheduledDate.scheduledFor) }
        }
  }

  private fun nextConfiguredAppointmentDate(
      latestAppointmentScheduledDate: PotentialAppointmentDate,
      configuredAppointmentScheduledDates: List<PotentialAppointmentDate>
  ): PotentialAppointmentDate {
    return configuredAppointmentScheduledDates
        .find { it > latestAppointmentScheduledDate }
        ?: throw RuntimeException("Cannot find configured appointment date after $latestAppointmentScheduledDate")
  }

  private fun previousConfiguredAppointmentDate(
      latestAppointmentScheduledDate: PotentialAppointmentDate,
      configuredAppointmentScheduledDates: List<PotentialAppointmentDate>
  ): PotentialAppointmentDate {
    return configuredAppointmentScheduledDates
        .findLast { it < latestAppointmentScheduledDate }
        ?: throw RuntimeException("Cannot find configured appointment date before ${latestAppointmentScheduledDate.scheduledFor}")
  }

  private fun scheduleAutomaticAppointmentForDefaulters(events: Observable<UiEvent>): Observable<UiChange> {
    val combinedStreams = Observables.combineLatest(
        events.ofType<SchedulingSkipped>(),
        patientUuid(events),
        configProvider
    )
    val isPatientDefaulterStream = combinedStreams
        .switchMap { (_, patientUuid) -> patientRepository.isPatientDefaulter(patientUuid) }
        .replay()
        .refCount()

    val saveAppointmentAndCloseSheet = isPatientDefaulterStream
        .filter { isPatientDefaulter -> isPatientDefaulter }
        .withLatestFrom(
            patientUuid(events),
            configProvider,
            currentFacilityStream()
        ) { _, patientUuid, config, currentFacilty ->
          Triple(patientUuid, config, currentFacilty)
        }
        .flatMapSingle { (patientUuid, config, currentFacility) ->
          scheduleAppointmentForPatient(
              uuid = patientUuid,
              date = LocalDate.now(clock).plus(config.appointmentDuePeriodForDefaulters),
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
        .flatMapSingle { (date, uuid, currentFacility) -> scheduleAppointmentForPatient(uuid, date.scheduledFor, currentFacility, Manual) }
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

  private fun generatePotentialAppointmentDate(
      appointmentDate: LocalDate,
      potentialAppointmentDates: List<PotentialAppointmentDate>
  ): PotentialAppointmentDate {
    val timeToAppointment = coerceTimeToAppointmentFromPotentialsForCalendarDate(potentialAppointmentDates, appointmentDate)
    return PotentialAppointmentDate(appointmentDate, timeToAppointment)
  }

  private fun coerceTimeToAppointmentFromPotentialsForCalendarDate(
      potentialAppointmentDates: List<PotentialAppointmentDate>,
      date: LocalDate
  ): TimeToAppointment {
    val today = LocalDate.now(clock)
    val exactMatchingTimeToAppointment = potentialAppointmentDates
        .find { potentialAppointmentDate -> potentialAppointmentDate.scheduledFor == date }
        ?.timeToAppointment

    return exactMatchingTimeToAppointment ?: Days(ChronoUnit.DAYS.between(today, date).toInt())
  }

  private fun generatePotentialAppointmentDate(scheduleAppointmentIn: TimeToAppointment): PotentialAppointmentDate {
    val today = LocalDate.now(clock)
    return PotentialAppointmentDate(today.plus(scheduleAppointmentIn), scheduleAppointmentIn)
  }

  private fun generatePotentialAppointmentDates(scheduleAppointmentsIn: List<TimeToAppointment>): List<PotentialAppointmentDate> {
    val today = LocalDate.now(clock)
    return scheduleAppointmentsIn
        .map { timeToAppointment -> today.plus(timeToAppointment) to timeToAppointment }
        .map { (appointmentDate, timeToAppointment) -> PotentialAppointmentDate(appointmentDate, timeToAppointment) }
  }

  private data class PotentialAppointmentDate(
      val scheduledFor: LocalDate,
      val timeToAppointment: TimeToAppointment
  ) : Comparable<PotentialAppointmentDate> {
    override fun compareTo(other: PotentialAppointmentDate): Int {
      return this.scheduledFor.compareTo(other.scheduledFor)
    }
  }
}

private fun LocalDate.plus(timeToAppointment: TimeToAppointment): LocalDate {
  return this.plus(
      timeToAppointment.value.toLong(),
      when (timeToAppointment) {
        is Days -> ChronoUnit.DAYS
        is Weeks -> ChronoUnit.WEEKS
        is Months -> ChronoUnit.MONTHS
      }
  )
}
