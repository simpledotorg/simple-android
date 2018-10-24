package org.simple.clinic.patient.fuzzy

import dagger.Module
import dagger.Provides
import org.threeten.bp.Clock

@Module
open class AgeFuzzerModule {

  @Provides
  open fun provideAgeFuzzer(clock: Clock): AgeFuzzer {
    val fuzzinessFactor = 0.1F
    return PercentageFuzzer(clock, fuzzinessFactor)
  }
}
