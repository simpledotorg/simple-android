package org.simple.clinic.summary

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.AppDatabase

@Module
open class PatientSummaryModule {

  @Provides
  open fun providesSummaryConfig(): Single<PatientSummaryConfig> = Single.just(PatientSummaryConfig(
      numberOfBpPlaceholders = 3,
      numberOfBpsToDisplay = 100
  ))

  @Provides
  fun missingPhoneReminderDao(appDatabase: AppDatabase) = appDatabase.missingPhoneReminderDao()
}
