package org.simple.clinic.scheduleappointment

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
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
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.overdue.TimeToAppointment.Days
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.plus
import org.simple.clinic.util.toOptional
import org.simple.clinic.util.unwrapJust
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.util.UUID

typealias Ui = ScheduleAppointmentSheet
typealias UiChange = (Ui) -> Unit

class ScheduleAppointmentSheetController @AssistedInject constructor(
    @Assisted val patientUuid: UUID,
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val config: AppointmentConfig,
    private val clock: UserClock,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val protocolRepository: ProtocolRepository
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(patientUuid: UUID): ScheduleAppointmentSheetController
  }

  private val latestAppointmentDateScheduledSubject = BehaviorSubject.create<PotentialAppointmentDate>()

  private lateinit var allPotentialAppointmentDates: List<PotentialAppointmentDate>

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    allPotentialAppointmentDates = generatePotentialAppointmentDatesForScheduling()

    return Observable.mergeArray(
        scheduleAppointments(replayedEvents),
        enableIncrements(),
        enableDecrements(),
        showManualAppointmentDateSelector(replayedEvents),
        scheduleAutomaticAppointmentForDefaulters(replayedEvents),
        scheduleManualAppointment(replayedEvents),
        showPatientDefaultFacility(replayedEvents),
        showPatientSelectedFacility(replayedEvents)
    )
  }

  private fun generatePotentialAppointmentDatesForScheduling(): List<PotentialAppointmentDate> {
    return PotentialAppointmentDate.from(config.scheduleAppointmentsIn, clock)
        .distinctBy(PotentialAppointmentDate::scheduledFor)
        .sorted()
  }

  private fun scheduleAppointments(
      events: Observable<UiEvent>
  ): Observable<UiChange> {

    return Observable
        .merge(
            scheduleDefaultAppointmentDateForSheetCreates(events),
            scheduleOnNextConfiguredAppointmentDate(events),
            scheduleOnPreviousConfiguredAppointmentDate(events),
            scheduleOnExactDate(events)
        )
        .distinctUntilChanged()
        .doOnNext(latestAppointmentDateScheduledSubject::onNext)
        .map { appointmentDate -> { ui: Ui -> ui.updateScheduledAppointment(appointmentDate.scheduledFor, appointmentDate.timeToAppointment) } }
  }

  private fun scheduleOnExactDate(
      events: Observable<UiEvent>
  ): Observable<PotentialAppointmentDate> {
    return events
        .ofType<AppointmentCalendarDateSelected>()
        .map { it.selectedDate }
        .map(::generatePotentialAppointmentDate)
  }

  private fun scheduleOnPreviousConfiguredAppointmentDate(
      events: Observable<UiEvent>
  ): Observable<PotentialAppointmentDate> {
    return events
        .ofType<AppointmentDateDecremented>()
        .withLatestFrom(latestAppointmentDateScheduledSubject) { _, lastScheduledAppointmentDate ->
          previousConfiguredAppointmentDate(lastScheduledAppointmentDate)
        }
  }

  private fun scheduleOnNextConfiguredAppointmentDate(
      events: Observable<UiEvent>
  ): Observable<PotentialAppointmentDate> {
    return events
        .ofType<AppointmentDateIncremented>()
        .withLatestFrom(latestAppointmentDateScheduledSubject) { _, lastScheduledAppointmentDate ->
          nextConfiguredAppointmentDate(lastScheduledAppointmentDate)
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

  private fun enableIncrements(): Observable<UiChange> {
    return latestAppointmentDateScheduledSubject
        .map { latestAppointmentScheduledDate ->
          latestAppointmentScheduledDate < allPotentialAppointmentDates.last()
        }
        .distinctUntilChanged()
        .map { enable -> { ui: Ui -> ui.enableIncrementButton(enable) } }
  }

  private fun enableDecrements(): Observable<UiChange> {
    return latestAppointmentDateScheduledSubject
        .map { latestAppointmentScheduledDate ->
          latestAppointmentScheduledDate > allPotentialAppointmentDates.first()
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
      latestAppointmentScheduledDate: PotentialAppointmentDate
  ): PotentialAppointmentDate {
    return allPotentialAppointmentDates
        .find { it > latestAppointmentScheduledDate }
        ?: throw RuntimeException("Cannot find configured appointment date after $latestAppointmentScheduledDate")
  }

  private fun previousConfiguredAppointmentDate(
      latestAppointmentScheduledDate: PotentialAppointmentDate
  ): PotentialAppointmentDate {
    return allPotentialAppointmentDates
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
      appointmentDate: LocalDate
  ): PotentialAppointmentDate {
    val timeToAppointment = coerceTimeToAppointmentFromPotentialsForCalendarDate(appointmentDate)
    return PotentialAppointmentDate(appointmentDate, timeToAppointment)
  }

  private fun coerceTimeToAppointmentFromPotentialsForCalendarDate(
      date: LocalDate
  ): TimeToAppointment {
    val today = LocalDate.now(clock)
    val exactMatchingTimeToAppointment = allPotentialAppointmentDates
        .find { potentialAppointmentDate -> potentialAppointmentDate.scheduledFor == date }
        ?.timeToAppointment

    return exactMatchingTimeToAppointment ?: Days(ChronoUnit.DAYS.between(today, date).toInt())
  }

  private fun generatePotentialAppointmentDate(scheduleAppointmentIn: TimeToAppointment): PotentialAppointmentDate {
    val today = LocalDate.now(clock)
    return PotentialAppointmentDate(today.plus(scheduleAppointmentIn), scheduleAppointmentIn)
  }

  data class OngoingAppointment(
      val patientUuid: UUID,
      val appointmentDate: LocalDate,
      val appointmentFacilityUuid: UUID,
      val creationFacilityUuid: UUID
  )
}
