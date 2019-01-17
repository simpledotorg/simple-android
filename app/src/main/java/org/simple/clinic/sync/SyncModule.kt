package org.simple.clinic.sync

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.bp.BloodPressureModule
import org.simple.clinic.drugs.PrescriptionModule
import org.simple.clinic.facility.FacilityModule
import org.simple.clinic.medicalhistory.MedicalHistoryModule
import org.simple.clinic.overdue.AppointmentModule
import org.simple.clinic.overdue.communication.CommunicationModule
import org.simple.clinic.patient.sync.PatientSyncModule
import org.simple.clinic.protocol.ProtocolModule
import javax.inject.Named

@Module(includes = [
  PatientSyncModule::class,
  BloodPressureModule::class,
  PrescriptionModule::class,
  FacilityModule::class,
  AppointmentModule::class,
  CommunicationModule::class,
  MedicalHistoryModule::class,
  ProtocolModule::class])
class SyncModule {

  fun syncConfig(): Single<SyncConfig> {
    return Single.just(SyncConfig(
        syncInterval = SyncInterval.FREQUENT,
        batchSizeEnum = BatchSize.SMALL))
  }

  @Provides
  @Named("sync_config_frequent")
  fun frequentSyncConfig(): Single<SyncConfig> {
    return Single.just(SyncConfig(
        syncInterval = SyncInterval.FREQUENT,
        batchSizeEnum = BatchSize.MEDIUM))
  }

  @Provides
  @Named("sync_config_daily")
  fun dailySyncConfig(): Single<SyncConfig> {
    return Single.just(SyncConfig(
        syncInterval = SyncInterval.DAILY,
        batchSizeEnum = BatchSize.MEDIUM
    ))
  }
}
