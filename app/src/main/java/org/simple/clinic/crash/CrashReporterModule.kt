package org.simple.clinic.crash

import dagger.Module
import dagger.Provides

@Module
class CrashReporterModule {

  @Provides
  fun crashReporter(): CrashReporter = SentryCrashReporter()
}
