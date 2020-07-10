package org.simple.clinic.security.pin

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import java.time.Duration

@Module
class BruteForceProtectionConfigModule {

  @Provides
  fun config(): Observable<BruteForceProtectionConfig> {
    return Observable.just(BruteForceProtectionConfig(limitOfFailedAttempts = 5, blockDuration = Duration.ofMinutes(20)))
  }
}
