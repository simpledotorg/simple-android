package org.simple.clinic.summary

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.summary.bloodpressures.BloodPressureSummaryViewConfig
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryConfigModule
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultFacilityInfoApi
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordModule
import retrofit2.Retrofit
import javax.inject.Named

@Module(includes = [BloodSugarSummaryConfigModule::class, TeleconsultRecordModule::class])
class PatientSummaryModule {

  @Provides
  fun teleconsultationFacilitySyncApi(@Named("for_deployment") retrofit: Retrofit): TeleconsultFacilityInfoApi {
    return retrofit.create(TeleconsultFacilityInfoApi::class.java)
  }

  @Provides
  fun providesBloodPressureSummaryConfig(configReader: ConfigReader): BloodPressureSummaryViewConfig {
    return BloodPressureSummaryViewConfig.read(configReader)
  }

  @Provides
  fun missingPhoneReminderDao(appDatabase: AppDatabase) = appDatabase.missingPhoneReminderDao()

  @Provides
  fun teleconsultationFacilityDao(appDatabase: AppDatabase) = appDatabase.teleconsultFacilityInfoDao()

  @Provides
  fun medicalOfficersDao(appDatabase: AppDatabase) = appDatabase.teleconsultMedicalOfficersDao()

  @Provides
  fun teleconsultationFacilityWithMedicalOfficersDao(appDatabase: AppDatabase) = appDatabase.teleconsultFacilityWithMedicalOfficersDao()
}
