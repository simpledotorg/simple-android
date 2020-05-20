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
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.unwrapJust
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
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
    private val currentFacility: Lazy<Facility>,
    private val schedulers: SchedulersProvider
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(
        patientUuid: UUID,
        modelSupplier: () -> ScheduleAppointmentModel
    ): ScheduleAppointmentSheetController
  }

  private val model: ScheduleAppointmentModel
    get() = modelSupplier()

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()


    return Observable.mergeArray(
        scheduleAutomaticAppointmentForDefaulters(replayedEvents),
        scheduleManualAppointment(replayedEvents),
        showPatientDefaultFacility(replayedEvents),
        showPatientSelectedFacility(replayedEvents)
    )
  }

  private fun scheduleAutomaticAppointmentForDefaulters(events: Observable<UiEvent>): Observable<UiChange> {
    val schedulingSkippedStream = events.ofType<SchedulingSkipped>()

    val isPatientDefaulterStream = schedulingSkippedStream
        .switchMap { patientRepository.isPatientDefaulter(patientUuid) }
        .replay()
        .refCount()

    val appointmentStream = schedulingSkippedStream
        .map {
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

    val currentFacilityUuid = Observable
        .fromCallable { currentFacility.get().uuid }
        .subscribeOn(schedulers.io())
    val patientFacilityUuidStream = currentFacilityUuid.mergeWith(facilityChanged)

    return events
        .ofType<AppointmentDone>()
        .withLatestFrom(patientFacilityUuidStream) { _, patientFacilityUuid ->
          OngoingAppointment(
              patientUuid = patientUuid,
              appointmentDate = model.selectedAppointmentDate!!.scheduledFor,
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
        .observeOn(schedulers.io())
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

  data class OngoingAppointment(
      val patientUuid: UUID,
      val appointmentDate: LocalDate,
      val appointmentFacilityUuid: UUID,
      val creationFacilityUuid: UUID
  )
}
