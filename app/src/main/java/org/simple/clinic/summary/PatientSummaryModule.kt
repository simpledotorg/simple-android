package org.simple.clinic.summary

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewConfig

@Module
class PatientSummaryModule {

  @Provides
  fun providesSummaryConfig(configReader: ConfigReader): PatientSummaryConfig {
    return PatientSummaryConfig.read(configReader)
  }

  @Provides
  fun providesNewBloodPressureSummaryConfig(configReader: ConfigReader): NewBloodPressureSummaryViewConfig {
    return NewBloodPressureSummaryViewConfig.read(configReader)
  }

  @Provides
  fun missingPhoneReminderDao(appDatabase: AppDatabase) = appDatabase.missingPhoneReminderDao()
}
