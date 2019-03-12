package org.simple.clinic.sync

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.bp.BloodPressureModule
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.sync.BloodPressureSync
import org.simple.clinic.drugs.PrescriptionModule
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.drugs.sync.PrescriptionSync
import org.simple.clinic.facility.FacilityModule
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.medicalhistory.MedicalHistoryModule
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.medicalhistory.sync.MedicalHistorySync
import org.simple.clinic.overdue.AppointmentModule
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.AppointmentSync
import org.simple.clinic.overdue.communication.CommunicationModule
import org.simple.clinic.overdue.communication.CommunicationRepository
import org.simple.clinic.overdue.communication.CommunicationSync
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.sync.PatientSync
import org.simple.clinic.patient.sync.PatientSyncModule
import org.simple.clinic.protocol.ProtocolModule
import org.simple.clinic.protocol.sync.ProtocolSync
import org.simple.clinic.reports.ReportsModule
import org.simple.clinic.reports.ReportsSync
import javax.inject.Named

@Module(includes = [
  PatientSyncModule::class,
  BloodPressureModule::class,
  PrescriptionModule::class,
  FacilityModule::class,
  AppointmentModule::class,
  CommunicationModule::class,
  MedicalHistoryModule::class,
  ProtocolModule::class,
  ReportsModule::class])
class SyncModule {

  @Provides
  @Named("sync_config_frequent")
  fun frequentSyncConfig(): Single<SyncConfig> {
    return Single.just(SyncConfig(
        syncInterval = SyncInterval.FREQUENT,
        batchSize = BatchSize.LARGE,
        syncGroup = SyncGroup.FREQUENT))
  }

  @Provides
  @Named("sync_config_daily")
  fun dailySyncConfig(): Single<SyncConfig> {
    return Single.just(SyncConfig(
        syncInterval = SyncInterval.DAILY,
        batchSize = BatchSize.LARGE,
        syncGroup = SyncGroup.DAILY))
  }

  @Provides
  fun syncs(
      facilitySync: FacilitySync,
      protocolSync: ProtocolSync,
      patientSync: PatientSync,
      bloodPressureSync: BloodPressureSync,
      medicalHistorySync: MedicalHistorySync,
      appointmentSync: AppointmentSync,
      communicationSync: CommunicationSync,
      prescriptionSync: PrescriptionSync,
      reportsSync: ReportsSync
  ): ArrayList<ModelSync> {
    return arrayListOf(
        facilitySync, protocolSync, patientSync,
        bloodPressureSync, medicalHistorySync, appointmentSync,
        communicationSync, prescriptionSync, reportsSync
    )
  }

  @Provides
  @Named("frequently_syncing_repositories")
  fun syncRepositories(
      patientSyncRepository: PatientRepository,
      bloodPressureSyncRepository: BloodPressureRepository,
      medicalHistorySyncRepository: MedicalHistoryRepository,
      appointmentSyncRepository: AppointmentRepository,
      communicationSyncRepository: CommunicationRepository,
      prescriptionSyncRepository: PrescriptionRepository
  ): ArrayList<SynceableRepository<*, *>> {
    return arrayListOf(
        patientSyncRepository,
        bloodPressureSyncRepository,
        medicalHistorySyncRepository,
        appointmentSyncRepository,
        communicationSyncRepository,
        prescriptionSyncRepository
    )
  }
}
