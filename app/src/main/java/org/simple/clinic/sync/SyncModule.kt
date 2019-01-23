package org.simple.clinic.sync

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.bp.BloodPressureModule
import org.simple.clinic.bp.sync.BloodPressureSync
import org.simple.clinic.drugs.PrescriptionModule
import org.simple.clinic.drugs.sync.PrescriptionSync
import org.simple.clinic.facility.FacilityModule
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.medicalhistory.MedicalHistoryModule
import org.simple.clinic.medicalhistory.sync.MedicalHistorySync
import org.simple.clinic.overdue.AppointmentModule
import org.simple.clinic.overdue.AppointmentSync
import org.simple.clinic.overdue.communication.CommunicationModule
import org.simple.clinic.overdue.communication.CommunicationSync
import org.simple.clinic.patient.sync.PatientSync
import org.simple.clinic.patient.sync.PatientSyncModule
import org.simple.clinic.protocol.ProtocolModule
import org.simple.clinic.protocol.sync.ProtocolSync
import org.simple.clinic.reports.ReportsModule
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
        batchSize = BatchSize.MEDIUM,
        syncGroupId = "sync_group_frequent"))
  }

  @Provides
  @Named("sync_config_daily")
  fun dailySyncConfig(): Single<SyncConfig> {
    return Single.just(SyncConfig(
        syncInterval = SyncInterval.DAILY,
        batchSize = BatchSize.MEDIUM,
        syncGroupId = "sync_group_daily"))
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
      prescriptionSync: PrescriptionSync
  ): ArrayList<ModelSync> {
    return arrayListOf(
        facilitySync, protocolSync, patientSync,
        bloodPressureSync, medicalHistorySync, appointmentSync,
        communicationSync, prescriptionSync
    )
  }
}
