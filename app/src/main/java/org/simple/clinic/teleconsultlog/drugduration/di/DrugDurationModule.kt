package org.simple.clinic.teleconsultlog.drugduration.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.teleconsultlog.drugduration.DrugDurationConfig
import java.time.Duration

@Module
object DrugDurationModule {

  @Provides
  fun providesDrugDurationConfig(): DrugDurationConfig {
    return DrugDurationConfig(maxAllowedDuration = Duration.ofDays(1000))
  }
}
