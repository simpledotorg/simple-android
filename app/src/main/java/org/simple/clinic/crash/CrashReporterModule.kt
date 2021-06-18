package org.simple.clinic.crash

import dagger.Module
import dagger.Provides
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.platform.crash.CrashReporter_Old

@Module
class CrashReporterModule {

  @Provides
  fun crashReporter(): CrashReporter_Old {
    return CrashReporter
  }
}
