package org.simple.clinic.summary

import dagger.Module
import dagger.Provides

@Module
class PatientSummaryModule {

  @Provides
  fun providesSummaryConfig() = PatientSummaryConfig(numberOfBpPlaceholders = 3)
}
