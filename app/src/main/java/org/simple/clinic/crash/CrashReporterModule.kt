package org.simple.clinic.crash

import dagger.Module
import dagger.Provides
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession

@Module
open class CrashReporterModule {

  @Provides
  open fun crashReporter(userSession: UserSession, facilityRepository: FacilityRepository): CrashReporter {
    return NoOpCrashReporter()
  }
}
