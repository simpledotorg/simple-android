package org.simple.clinic.sync

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.BuildConfig
import org.simple.clinic.bp.BloodPressureModule
import org.simple.clinic.di.AppScope
import org.simple.clinic.drugs.PrescriptionModule
import org.simple.clinic.facility.FacilityModule
import org.simple.clinic.medicalhistory.MedicalHistoryModule
import org.simple.clinic.overdue.AppointmentModule
import org.simple.clinic.overdue.communication.CommunicationModule
import org.simple.clinic.patient.sync.PatientSyncModule
import org.threeten.bp.Duration
import retrofit2.Retrofit

@Module(includes = [
  PatientSyncModule::class,
  BloodPressureModule::class,
  PrescriptionModule::class,
  FacilityModule::class,
  AppointmentModule::class,
  CommunicationModule::class,
  MedicalHistoryModule::class])
open class SyncModule {

  // TODO: move to NetworkModule.
  @Provides
  @AppScope
  fun retrofit(commonRetrofitBuilder: Retrofit.Builder): Retrofit {
    val baseUrl = BuildConfig.API_ENDPOINT
    return commonRetrofitBuilder
        .baseUrl(baseUrl)
        .build()
  }

  @Provides
  open fun syncConfig(): Single<SyncConfig> {
    return Single.just(
        SyncConfig(
            frequency = Duration.ofMinutes(16),
            backOffDelay = Duration.ofMinutes(5),
            batchSize = 50))
  }
}
