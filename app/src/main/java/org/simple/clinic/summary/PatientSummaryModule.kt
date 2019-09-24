package org.simple.clinic.summary

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.simple.clinic.AppDatabase
import org.simple.clinic.remoteconfig.ConfigReader

@Module
open class PatientSummaryModule {

  @Provides
  open fun providesSummaryConfig(configReader: ConfigReader): Observable<PatientSummaryConfig> =
      PatientSummaryConfig.read(configReader)

  @Provides
  fun missingPhoneReminderDao(appDatabase: AppDatabase) = appDatabase.missingPhoneReminderDao()
}
