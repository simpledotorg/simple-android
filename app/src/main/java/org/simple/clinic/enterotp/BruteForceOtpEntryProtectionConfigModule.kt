package org.simple.clinic.enterotp

import dagger.Module
import dagger.Provides
import java.time.Duration

@Module
class BruteForceOtpEntryProtectionConfigModule {
  @Provides
  fun config(): BruteForceOtpEntryProtectionConfig {
    return BruteForceOtpEntryProtectionConfig(limitOfFailedAttempts = 4, blockDuration = Duration.ofMinutes(20))
  }
}
