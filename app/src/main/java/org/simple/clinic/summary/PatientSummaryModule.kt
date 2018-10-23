package org.simple.clinic.summary

import dagger.Module
import dagger.Provides
import org.threeten.bp.Duration

@Module
class PatientSummaryModule {

  @Provides
  fun providesSummaryConfig() = PatientSummaryConfig(numberOfBpPlaceholders = 3, bpEditableFor = Duration.ofMinutes(1L))
}
