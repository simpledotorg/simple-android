package org.simple.clinic.enterotp

import dagger.Module
import dagger.Provides
import java.time.Duration

@Module
class BruteForceOtpEntryProtectionConfigModule {
  @Provides
  fun config(): BruteForceOtpEntryProtectionConfig {
    return BruteForceOtpEntryProtectionConfig(limitOfFailedAttempts = 5, blockDuration = Duration.ofMinutes(20), minOtpEntries = 3)
  }
}
