package org.simple.clinic.di

import io.reactivex.Observable
import org.simple.clinic.security.pin.BruteForceProtectionConfig
import org.simple.clinic.security.pin.BruteForceProtectionModule
import org.threeten.bp.Duration

class DebugBruteForceProtectionModule : BruteForceProtectionModule() {
  override fun config(): Observable<BruteForceProtectionConfig> {
    return super.config().map {
      it.copy(blockDuration = Duration.ofSeconds(5))
    }
  }
}
