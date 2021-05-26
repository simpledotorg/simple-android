package org.simple.clinic.security.pin

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import org.simple.clinic.util.preference.MoshiObjectPreferenceConverter

@Module(includes = [BruteForceProtectionConfigModule::class])
open class BruteForceProtectionModule {

  @Provides
  fun state(
      rxSharedPrefs: RxSharedPreferences,
      moshi: Moshi
  ): Preference<BruteForceProtectionState> {
    val typeConverter = MoshiObjectPreferenceConverter(moshi, BruteForceProtectionState::class.java)
    return rxSharedPrefs.getObject("brute_force_state_v1", BruteForceProtectionState(), typeConverter)
  }
}
