package org.simple.clinic.enterotp

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import org.simple.clinic.util.preference.MoshiObjectPreferenceConverter

@Module(includes = [BruteForceOtpEntryProtectionConfigModule::class])
class BruteForceOtpEntryProtectionModule {

  @Provides
  fun otpAttempts(
      rxSharedPrefs: RxSharedPreferences,
      moshi: Moshi
  ): Preference<BruteForceOtpProtectionState> {
    val typeConverter = MoshiObjectPreferenceConverter(moshi, BruteForceOtpProtectionState::class.java)
    return rxSharedPrefs.getObject("login_otp_attempt_limit", BruteForceOtpProtectionState(), typeConverter)
  }
}
