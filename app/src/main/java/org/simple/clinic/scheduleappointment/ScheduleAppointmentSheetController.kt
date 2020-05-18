package org.simple.clinic.scheduleappointment

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
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
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unwrapJust
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.util.UUID

typealias Ui = ScheduleAppointmentUi
typealias UiChange = (Ui) -> Unit

class ScheduleAppointmentSheetController @AssistedInject constructor(
    @Assisted private val patientUuid: UUID,
    @Assisted private val modelSupplier: () -> ScheduleAppointmentModel,
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val config: AppointmentConfig,
    private val clock: UserClock,
    private val facilityRepository: FacilityRepository,
    private val protocolRepository: ProtocolRepository,
    private val currentFacility: Lazy<Facility>
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(
        patientUuid: UUID,
        modelSupplier: () -> ScheduleAppointmentModel
    ): ScheduleAppointmentSheetController
  }

  private val allPotentialAppointmentDates: List<PotentialAppointmentDate>
    get() = model.potentialAppointmentDates

  private val model: ScheduleAppointmentModel
    get() = modelSupplier()

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()


    return Observable.mergeArray(
        showManualAppointmentDateSelector(replayedEvents),
        scheduleAutomaticAppointmentForDefaulters(replayedEvents),
        scheduleManualAppointment(replayedEvents),
        showPatientDefaultFacility(replayedEvents),
        showPatientSelectedFacility(replayedEvents),
        scheduleOnNextConfiguredAppointmentDate(events),
        scheduleOnPreviousConfiguredAppointmentDate(events),
        scheduleOnExactDate(events)
    )
  }

  private fun enableIncrements(ui: Ui) {
    val areLaterPotentialAppointmentsAvailable = model.selectedAppointmentDate!!.date < allPotentialAppointmentDates.last()

    ui.enableIncrementButton(areLaterPotentialAppointmentsAvailable)
  }

  private fun enableDecrements(ui: Ui) {
    val areEarlierPotentialAppointmentsAvailable = model.selectedAppointmentDate!!.date > allPotentialAppointmentDates.first()

    ui.enableDecrementButton(areEarlierPotentialAppointmentsAvailable)
  }

  private fun scheduleOnExactDate(
      events: Observable<UiEvent>
  ): Observable<UiChange> {
    return events
        .ofType<AppointmentCalendarDateSelected>()
        .map { it.selectedDate }
        .map(::generatePotentialAppointmentDate)
        .doOnNext { model.selectedAppointmentDate!!.date = it }
        .map { appointmentDate ->
          { ui: Ui ->
            ui.updateScheduledAppointment(appointmentDate.scheduledFor, appointmentDate.timeToAppointment)
            enableIncrements(ui)
            enableDecrements(ui)
          }
        }
  }

  private fun scheduleOnPreviousConfiguredAppointmentDate(
      events: Observable<UiEvent>
  ): Observable<UiChange> {
    return events
        .ofType<AppointmentDateDecremented>()
        .map { previousConfiguredAppointmentDate(model.selectedAppointmentDate!!.date) }
        .doOnNext { model.selectedAppointmentDate!!.date = it }
        .map { appointmentDate ->
          { ui: Ui ->
            ui.updateScheduledAppointment(appointmentDate.scheduledFor, appointmentDate.timeToAppointment)
            enableIncrements(ui)
            enableDecrements(ui)
          }
        }
  }

  private fun scheduleOnNextConfiguredAppointmentDate(
      events: Observable<UiEvent>
  ): Observable<UiChange> {
    return events
        .ofType<AppointmentDateIncremented>()
        .map { nextConfiguredAppointmentDate(model.selectedAppointmentDate!!.date) }
        .doOnNext { model.selectedAppointmentDate!!.date = it }
        .map { appointmentDate ->
          { ui: Ui ->
            ui.updateScheduledAppointment(appointmentDate.scheduledFor, appointmentDate.timeToAppointment)
            enableIncrements(ui)
            enableDecrements(ui)
          }
        }
  }

  private fun showManualAppointmentDateSelector(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ManuallySelectAppointmentDateClicked>()
        .map {
          { ui: Ui -> ui.showManualDateSelector(model.selectedAppointmentDate!!.date.scheduledFor) }
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
        .withLatestFrom(patientFacilityUuidStream) { _, patientFacilityUuid ->
          OngoingAppointment(
              patientUuid = patientUuid,
              appointmentDate = model.selectedAppointmentDate!!.date.scheduledFor,
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

  data class OngoingAppointment(
      val patientUuid: UUID,
      val appointmentDate: LocalDate,
      val appointmentFacilityUuid: UUID,
      val creationFacilityUuid: UUID
  )
}
