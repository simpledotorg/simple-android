package org.simple.clinic.di

import org.simple.clinic.crash.CrashReporterModule
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.platform.crash.NoOpCrashReporter
import org.simple.clinic.user.UserSession

class DebugCrashReporterModule : CrashReporterModule() {
  override fun crashReporter(
      userSession: UserSession,
      facilityRepository: FacilityRepository
  ): NoOpCrashReporter {
    return NoOpCrashReporter()
  }
}
