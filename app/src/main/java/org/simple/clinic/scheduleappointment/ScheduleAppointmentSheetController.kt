package org.simple.clinic.scheduleappointment

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
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
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.scheduleappointment.TimeToAppointment.Days
import org.simple.clinic.scheduleappointment.TimeToAppointment.Months
import org.simple.clinic.scheduleappointment.TimeToAppointment.Weeks
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.toOptional
import org.simple.clinic.util.unwrapJust
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
    private val config: AppointmentConfig,
    private val clock: UserClock,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val protocolRepository: ProtocolRepository
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
        scheduleManualAppointment(replayedEvents),
        showPatientDefaultFacility(replayedEvents),
        showPatientSelectedFacility(replayedEvents)
    )
  }

  private fun generatePotentialAppointmentDatesForScheduling(): Observable<List<PotentialAppointmentDate>> {
    return Observable.fromCallable { generatePotentialAppointmentDates(config.scheduleAppointmentsIn) }
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
    val protocolStream = currentFacilityStream()
        .map { it.protocolUuid.toOptional() }
        .filterAndUnwrapJust()
        .switchMap(protocolRepository::protocol)

    val configTimeToAppointment = Observable.fromCallable { config.defaultTimeToAppointment }
    val protocolTimeToAppointment = protocolStream.map { Days(it.followUpDays) }

    val timeToAppointments = Observable.concatArrayEager(configTimeToAppointment, protocolTimeToAppointment)

    return Observables.combineLatest(
        events.ofType<ScheduleAppointmentSheetCreated>(),
        timeToAppointments
    ) { _, timeToAppointment ->
      timeToAppointment
    }
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
    val combinedStreams = Observables.combineLatest(events.ofType<SchedulingSkipped>(), patientUuid(events))

    val isPatientDefaulterStream = combinedStreams
        .switchMap { (_, patientUuid) -> patientRepository.isPatientDefaulter(patientUuid) }
        .replay()
        .refCount()

    val appointmentStream = Observables
        .combineLatest(patientUuid(events), currentFacilityStream()) { uuid, currentFacility ->
          OngoingAppointment(
              patientUuid = uuid,
              appointmentDate = LocalDate.now(clock).plus(config.appointmentDuePeriodForDefaulters),
              appointmentFacilityUuid = currentFacility.uuid,
              creationFacilityUuid = currentFacility.uuid
          )
        }

    val saveAppointmentAndCloseSheet = isPatientDefaulterStream
        .filter { isPatientDefaulter -> isPatientDefaulter }
        .withLatestFrom(appointmentStream)
        .flatMapSingle { (_, appointment) ->
          appointmentRepository.schedule(
              patientUuid = appointment.patientUuid,
              appointmentUuid = UUID.randomUUID(),
              appointmentDate = appointment.appointmentDate,
              appointmentFacilityUuid = appointment.appointmentFacilityUuid,
              appointmentType = Automatic,
              creationFacilityUuid = appointment.creationFacilityUuid
          )
        }
        .map { Ui::closeSheet }

    val closeSheetWithoutSavingAppointment = isPatientDefaulterStream
        .filter { isPatientDefaulter -> isPatientDefaulter.not() }
        .map { Ui::closeSheet }

    return Observable.merge(saveAppointmentAndCloseSheet, closeSheetWithoutSavingAppointment)
  }

  private fun scheduleManualAppointment(events: Observable<UiEvent>): Observable<UiChange> {
    val facilityChanged = events
        .ofType<PatientFacilityChanged>()
        .map { it.facilityUuid }

    val currentFacilityUuid = currentFacilityStream().map { it.uuid }
    val patientFacilityUuidStream = currentFacilityUuid.mergeWith(facilityChanged)

    val appointmentStream = Observables.combineLatest(
        patientUuid(events),
        latestAppointmentDateScheduledSubject,
        patientFacilityUuidStream,
        currentFacilityUuid) { uuid, date, facilityUuid, currentFacility ->
      OngoingAppointment(
          patientUuid = uuid,
          appointmentDate = date.scheduledFor,
          appointmentFacilityUuid = facilityUuid,
          creationFacilityUuid = currentFacility
      )
    }

    return events
        .ofType<AppointmentDone>()
        .withLatestFrom(appointmentStream)
        .flatMapSingle { (_, appointment) ->
          appointmentRepository
              .schedule(
                  patientUuid = appointment.patientUuid,
                  appointmentUuid = UUID.randomUUID(),
                  appointmentDate = appointment.appointmentDate,
                  appointmentType = Manual,
                  appointmentFacilityUuid = appointment.appointmentFacilityUuid,
                  creationFacilityUuid = appointment.creationFacilityUuid
              )
        }
        .map { Ui::closeSheet }
  }

  private fun showPatientDefaultFacility(events: Observable<UiEvent>): Observable<UiChange> {
    val creates = events
        .ofType<ScheduleAppointmentSheetCreated>()
    return Observables.combineLatest(creates, currentFacilityStream()) { _, facility -> { ui: Ui -> ui.showPatientFacility(facility.name) } }
  }

  private fun showPatientSelectedFacility(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientFacilityChanged>()
        .map { facilityRepository.facility(it.facilityUuid) }
        .unwrapJust()
        .map { { ui: Ui -> ui.showPatientFacility(it.name) } }
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

  data class OngoingAppointment(
      val patientUuid: UUID,
      val appointmentDate: LocalDate,
      val appointmentFacilityUuid: UUID,
      val creationFacilityUuid: UUID
  )
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
