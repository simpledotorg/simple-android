package org.simple.clinic.crash

import dagger.Module
import dagger.Provides

@Module
open class CrashReporterModule {

  @Provides
  open fun crashReporter(): CrashReporter = SentryCrashReporter()
}
