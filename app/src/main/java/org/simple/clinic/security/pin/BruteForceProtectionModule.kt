package org.simple.clinic.security.pin

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.util.InstantRxPreferencesConverter
import org.simple.clinic.util.Optional
import org.simple.clinic.util.OptionalRxPreferencesConverter
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import javax.inject.Named

@Module
open class BruteForceProtectionModule {

  @Provides
  open fun config(): Single<BruteForceProtectionConfig> {
    return Single.just(BruteForceProtectionConfig(limitOfFailedAttempts = 5, blockDuration = Duration.ofMinutes(20)))
  }

  @Provides
  @Named("pin_failed_auth_count")
  fun failedPinAuthenticationCount(rxSharedPrefs: RxSharedPreferences): Preference<Int> {
    return rxSharedPrefs.getInteger("pin_failed_auth_count", BruteForceProtection.defaultFailedAttemptsCount())
  }

  @Provides
  @Named("pin_failed_auth_limit_reached_at")
  fun attemptsReachedAtPreference(rxSharedPrefs: RxSharedPreferences): Preference<Optional<Instant>> {
    val converter = OptionalRxPreferencesConverter(InstantRxPreferencesConverter())
    return rxSharedPrefs.getObject("pin_failed_auth_limit_reached_at", BruteForceProtection.defaultAttemptsReachedAtTime(), converter)
  }
}
