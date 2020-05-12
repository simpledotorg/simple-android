package org.simple.clinic.summary

import androidx.paging.Config
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.summary.bloodpressures.BloodPressureSummaryViewConfig
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryConfigModule
import org.simple.clinic.summary.teleconsultation.api.TeleconsultationApi
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Named

@Module(includes = [BloodSugarSummaryConfigModule::class])
class PatientSummaryModule {

  @Provides
  fun providesTeleconsultaionApi(@Named("for_country") retrofit: Retrofit): TeleconsultationApi {
    return retrofit.create()
  }

  @Provides
  fun providesBloodPressureSummaryConfig(configReader: ConfigReader): BloodPressureSummaryViewConfig {
    return BloodPressureSummaryViewConfig.read(configReader)
  }

  @Provides
  fun missingPhoneReminderDao(appDatabase: AppDatabase) = appDatabase.missingPhoneReminderDao()

  @Provides
  fun measurementHistoryPaginationConfig() = Config(
      pageSize = 20,
      prefetchDistance = 10,
      initialLoadSizeHint = 40,
      enablePlaceholders = false
  )
}
