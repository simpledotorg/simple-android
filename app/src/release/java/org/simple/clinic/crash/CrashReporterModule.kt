package org.simple.clinic.crash

import dagger.Binds
import dagger.Module
import org.simple.clinic.platform.crash.CrashReporter

@Module
abstract class CrashReporterModule {

  @Binds
  abstract fun crashReporter(crashReporter: SentryCrashReporter): CrashReporter
}
