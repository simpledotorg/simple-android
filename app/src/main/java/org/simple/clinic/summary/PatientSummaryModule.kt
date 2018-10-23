package org.simple.clinic.summary

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.threeten.bp.Duration

@Module
class PatientSummaryModule {

  @Provides
  fun providesSummaryConfig(): Single<PatientSummaryConfig> = Single.just(PatientSummaryConfig(
      numberOfBpPlaceholders = 3,
      bpEditableFor = Duration.ofMinutes(1L)
  ))
}
