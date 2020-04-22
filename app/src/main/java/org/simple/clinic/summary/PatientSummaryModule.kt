package org.simple.clinic.summary

import androidx.paging.Config
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.summary.bloodpressures.BloodPressureSummaryViewConfig
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryConfigModule

@Module(includes = [BloodSugarSummaryConfigModule::class])
class PatientSummaryModule {

  @Provides
  fun providesSummaryConfig(configReader: ConfigReader): PatientSummaryConfig {
    return PatientSummaryConfig.read(configReader)
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
