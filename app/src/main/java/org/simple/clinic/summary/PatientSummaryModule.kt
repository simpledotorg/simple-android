package org.simple.clinic.summary

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.threeten.bp.Duration

@Module
open class PatientSummaryModule {

  @Provides
  open fun providesSummaryConfig(): Single<PatientSummaryConfig> = Single.just(PatientSummaryConfig(
      numberOfBpPlaceholders = 3,
      bpEditableFor = Duration.ofDays(1L),
      isPatientEditFeatureEnabled = true
  ))
}
