package org.simple.clinic.summary

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.simple.clinic.AppDatabase
import org.simple.clinic.remoteconfig.ConfigReader

@Module
class PatientSummaryModule {

  @Provides
  fun providesSummaryConfig(configReader: ConfigReader): Observable<PatientSummaryConfig> {
    return PatientSummaryConfig.read(configReader)
  }

  @Provides
  fun missingPhoneReminderDao(appDatabase: AppDatabase) = appDatabase.missingPhoneReminderDao()
}
