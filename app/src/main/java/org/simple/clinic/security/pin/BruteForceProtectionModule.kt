package org.simple.clinic.security.pin

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.threeten.bp.Duration

@Module
open class BruteForceProtectionModule {

  @Provides
  open fun config(): Single<BruteForceProtectionConfig> {
    return Single.just(BruteForceProtectionConfig(isEnabled = false, limitOfFailedAttempts = 5, blockDuration = Duration.ofMinutes(20)))
  }

  @Provides
  fun state(rxSharedPrefs: RxSharedPreferences, moshi: Moshi): Preference<BruteForceProtectionState> {
    val typeConverter = BruteForceProtectionState.RxPreferencesConverter(moshi)
    return rxSharedPrefs.getObject("brute_force_state", BruteForceProtectionState(), typeConverter)
  }
}
