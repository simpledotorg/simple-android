package org.simple.clinic.scheduleappointment

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.subjects.BehaviorSubject
import org.simple.clinic.ReplayUntilScreenIsDestroyed
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
import org.simple.clinic.util.plus
import org.simple.clinic.util.unwrapJust
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.util.UUID

typealias Ui = ScheduleAppointmentUi
typealias UiChange = (Ui) -> Unit

class ScheduleAppointmentSheetController @AssistedInject constructor(
    @Assisted val patientUuid: UUID,
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val config: AppointmentConfig,
    private val clock: UserClock,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val protocolRepository: ProtocolRepository,
    private val currentFacility: Lazy<Facility>
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(patientUuid: UUID): ScheduleAppointmentSheetController
  }

  private val latestAppointmentDateScheduledSubject = BehaviorSubject.create<PotentialAppointmentDate>()

  private lateinit var allPotentialAppointmentDates: List<PotentialAppointmentDate>

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    allPotentialAppointmentDates = generatePotentialAppointmentDatesForScheduling()

    return Observable.mergeArray(
        scheduleAppointments(replayedEvents),
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
        .map { appointmentDate ->
          { ui: Ui ->
            ui.updateScheduledAppointment(appointmentDate.scheduledFor, appointmentDate.timeToAppointment)
            enableIncrements(ui)
            enableDecrements(ui)
          }
        }
  }

  private fun enableIncrements(ui: Ui) {
    val latestAppointmentScheduledDate = latestAppointmentDateScheduledSubject.value!!
    val areLaterPotentialAppointmentsAvailable = latestAppointmentScheduledDate < allPotentialAppointmentDates.last()

    ui.enableIncrementButton(areLaterPotentialAppointmentsAvailable)
  }

  private fun enableDecrements(ui: Ui) {
    val latestAppointmentScheduledDate = latestAppointmentDateScheduledSubject.value!!
    val areEarlierPotentialAppointmentsAvailable = latestAppointmentScheduledDate > allPotentialAppointmentDates.first()

    ui.enableDecrementButton(areEarlierPotentialAppointmentsAvailable)
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
    val protocolUuid = currentFacility.get().protocolUuid!!
    val protocolStream = protocolRepository.protocol(protocolUuid)

    val configTimeToAppointment = Observable.fromCallable { config.defaultTimeToAppointment }
    val protocolTimeToAppointment = protocolStream.map { Days(it.followUpDays) }

    val timeToAppointments = Observable.concatArrayEager(configTimeToAppointment, protocolTimeToAppointment)

    return Observables.combineLatest(
        events.ofType<ScreenCreated>(),
        timeToAppointments
    ) { _, timeToAppointment ->
      timeToAppointment
    }
        .map(::generatePotentialAppointmentDate)
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
    val schedulingSkippedStream = events.ofType<SchedulingSkipped>()

    val isPatientDefaulterStream = schedulingSkippedStream
        .switchMap { patientRepository.isPatientDefaulter(patientUuid) }
        .replay()
        .refCount()

    val appointmentStream = schedulingSkippedStream.map {
      val currentFacilityUuid = currentFacility.get().uuid

      OngoingAppointment(
          patientUuid = patientUuid,
          appointmentDate = LocalDate.now(clock).plus(config.appointmentDuePeriodForDefaulters),
          appointmentFacilityUuid = currentFacilityUuid,
          creationFacilityUuid = currentFacilityUuid
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

    val currentFacilityUuid = Observable.fromCallable { currentFacility.get().uuid }
    val patientFacilityUuidStream = currentFacilityUuid.mergeWith(facilityChanged)

    return events
        .ofType<AppointmentDone>()
        .withLatestFrom(patientFacilityUuidStream, latestAppointmentDateScheduledSubject) { _, patientFacilityUuid, appointmentDate ->
          OngoingAppointment(
              patientUuid = patientUuid,
              appointmentDate = appointmentDate.scheduledFor,
              appointmentFacilityUuid = patientFacilityUuid,
              creationFacilityUuid = currentFacility.get().uuid
          )
        }
        .flatMapSingle { appointmentEntry ->
          appointmentRepository
              .schedule(
                  patientUuid = appointmentEntry.patientUuid,
                  appointmentUuid = UUID.randomUUID(),
                  appointmentDate = appointmentEntry.appointmentDate,
                  appointmentType = Manual,
                  appointmentFacilityUuid = appointmentEntry.appointmentFacilityUuid,
                  creationFacilityUuid = appointmentEntry.creationFacilityUuid
              )
        }
        .map { Ui::closeSheet }
  }

  private fun showPatientDefaultFacility(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .map { currentFacility.get() }
        .map { facility ->
          { ui: Ui ->
            ui.showPatientFacility(facility.name)
          }
        }
  }

  private fun showPatientSelectedFacility(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientFacilityChanged>()
        .map { facilityRepository.facility(it.facilityUuid) }
        .unwrapJust()
        .map { { ui: Ui -> ui.showPatientFacility(it.name) } }
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
