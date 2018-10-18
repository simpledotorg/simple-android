package org.simple.clinic.patient.fuzzy

import dagger.Module
import dagger.Provides

@Module
open class AgeFuzzerModule {

  @Provides
  open fun provideAgeFuzzer(): AgeFuzzer = AbsoluteFuzzer(5)
}
