package org.simple.clinic.enterotp

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import java.time.Duration

@Module
class BruteForceOtpEntryProtectionConfigModule {
  @Provides
  fun config(): Observable<BruteForceOtpEntryProtectionConfig> {
    return Observable.just(BruteForceOtpEntryProtectionConfig(limitOfFailedAttempts = 5, blockDuration = Duration.ofMinutes(20)))
  }
}
