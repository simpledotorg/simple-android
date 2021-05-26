package org.simple.clinic.scheduleappointment

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.protocol.Protocol
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.plus
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toNullable
import org.simple.clinic.util.toOptional
import org.simple.clinic.uuid.UuidGenerator
import java.time.LocalDate
import java.util.function.Function

class ScheduleAppointmentEffectHandler @AssistedInject constructor(
    private val currentFacility: Lazy<Facility>,
    private val protocolRepository: ProtocolRepository,
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val facilityRepository: FacilityRepository,
    private val appointmentConfig: AppointmentConfig,
    private val userClock: UserClock,
    private val schedulers: SchedulersProvider,
    private val uuidGenerator: UuidGenerator,
    @Assisted private val uiActions: ScheduleAppointmentUiActions,
    private val teleconsultRecordRepository: TeleconsultRecordRepository
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: ScheduleAppointmentUiActions): ScheduleAppointmentEffectHandler
  }

  fun build(): ObservableTransformer<ScheduleAppointmentEffect, ScheduleAppointmentEvent> {
    return RxMobius
        .subtypeEffectHandler<ScheduleAppointmentEffect, ScheduleAppointmentEvent>()
        .addTransformer(LoadDefaultAppointmentDate::class.java, loadDefaultAppointmentDate())
        .addConsumer(ShowDatePicker::class.java, { uiActions.showManualDateSelector(it.selectedDate) }, schedulers.ui())
        .addTransformer(LoadAppointmentFacilities::class.java, loadAppointmentFacility())
        .addTransformer(ScheduleAppointmentForPatient::class.java, scheduleAppointmentForPatient())
        .addAction(CloseSheet::class.java, uiActions::closeSheet, schedulers.ui())
        .addTransformer(LoadPatientDefaulterStatus::class.java, loadPatientDefaulterStatus())
        .addTransformer(LoadTeleconsultRecord::class.java, loadTeleconsultRecordDetails())
        .addConsumer(GoToTeleconsultStatusSheet::class.java, { uiActions.openTeleconsultStatusSheet(it.teleconsultRecordUuid) }, schedulers.ui())
        .addTransformer(ScheduleAppointmentForPatientFromNext::class.java, scheduleAppointmentForPatientFromNext())
        .build()
  }

  private fun scheduleAppointmentForPatientFromNext(): ObservableTransformer<ScheduleAppointmentForPatientFromNext, ScheduleAppointmentEvent> {
    return ObservableTransformer { effects ->
      effects
          .doOnNext { scheduleAppointment ->
            appointmentRepository.schedule(
                patientUuid = scheduleAppointment.patientUuid,
                appointmentUuid = uuidGenerator.v4(),
                appointmentDate = scheduleAppointment.scheduledForDate,
                appointmentType = scheduleAppointment.type,
                appointmentFacilityUuid = scheduleAppointment.scheduledAtFacility.uuid,
                creationFacilityUuid = currentFacility.get().uuid
            )
          }
          .map { AppointmentScheduledForPatientFromNext }
    }
  }

  private fun loadTeleconsultRecordDetails(): ObservableTransformer<LoadTeleconsultRecord, ScheduleAppointmentEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map {
            val teleconsultRecord = teleconsultRecordRepository.getPatientTeleconsultRecord(patientUuid = it.patientUuid)
            TeleconsultRecordLoaded(teleconsultRecord)
          }
    }
  }

  private fun loadDefaultAppointmentDate(): ObservableTransformer<LoadDefaultAppointmentDate, ScheduleAppointmentEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { currentProtocol(currentFacility.get()) }
          .map(::defaultTimeToAppointment)
          .map(::generatePotentialAppointmentDate)
          .map(::DefaultAppointmentDateLoaded)
    }
  }

  private fun currentProtocol(facility: Facility): Optional<Protocol> {
    return if (facility.protocolUuid == null)
      None()
    else
      protocolRepository.protocolImmediate(facility.protocolUuid).toOptional()
  }

  private fun defaultTimeToAppointment(protocol: Optional<Protocol>): TimeToAppointment {
    return if (protocol is Just) {
      TimeToAppointment.Days(protocol.value.followUpDays)
    } else {
      appointmentConfig.defaultTimeToAppointment
    }
  }

  private fun generatePotentialAppointmentDate(scheduleAppointmentIn: TimeToAppointment): PotentialAppointmentDate {
    val today = LocalDate.now(userClock)
    return PotentialAppointmentDate(today.plus(scheduleAppointmentIn), scheduleAppointmentIn)
  }

  private fun loadAppointmentFacility(): ObservableTransformer<LoadAppointmentFacilities, ScheduleAppointmentEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { patientRepository.patientImmediate(it.patientUuid) }
          .map {
            val assignedFacility = getAssignedFacility(it).toNullable()
            AppointmentFacilitiesLoaded(assignedFacility, currentFacility.get())
          }
    }
  }

  private fun getAssignedFacility(patient: Patient): Optional<Facility> {
    return Optional
        .ofNullable(patient.assignedFacilityId)
        .flatMap(Function { facilityRepository.facility(it) })
  }

  private fun scheduleAppointmentForPatient(): ObservableTransformer<ScheduleAppointmentForPatient, ScheduleAppointmentEvent> {
    return ObservableTransformer { effects ->
      effects
          .doOnNext { scheduleAppointment ->
            appointmentRepository.schedule(
                patientUuid = scheduleAppointment.patientUuid,
                appointmentUuid = uuidGenerator.v4(),
                appointmentDate = scheduleAppointment.scheduledForDate,
                appointmentType = scheduleAppointment.type,
                appointmentFacilityUuid = scheduleAppointment.scheduledAtFacility.uuid,
                creationFacilityUuid = currentFacility.get().uuid
            )
          }
          .map { AppointmentScheduled }
    }
  }

  private fun loadPatientDefaulterStatus(): ObservableTransformer<LoadPatientDefaulterStatus, ScheduleAppointmentEvent> {
    return ObservableTransformer { effects ->
      effects
          .map {
            val isPatientDefaulter = patientRepository.isPatientDefaulter(it.patientUuid)
            PatientDefaulterStatusLoaded(isPatientDefaulter)
          }
    }
  }
}
