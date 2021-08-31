package org.simple.clinic.enterotp

import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.util.UtcClock
import javax.inject.Inject

class BruteForceOtpEntryProtection @Inject constructor(
    private val utcClock: UtcClock,
    private val config: BruteForceOtpEntryProtectionConfig,
    private val otpStatePreference: Preference<BruteForceOtpProtectionState>
) {

  fun incrementFailedOtpAttempt() {
    val updatedBruteForceOtpProtectionState = updateFailedOtpAttemptLimitReached(config.limitOfFailedAttempts)
    otpStatePreference.set(updatedBruteForceOtpProtectionState)
  }

  private fun updateFailedOtpAttemptLimitReached(
      maxAllowedFailedOtpAttempts: Int
  ): BruteForceOtpProtectionState {
    val otpState = otpStatePreference.get()
    val isOtpAttemptLimitReached = otpState.failedLoginOtpAttempt >= maxAllowedFailedOtpAttempts
    return if (isOtpAttemptLimitReached && !otpState.limitReachedAt.isPresent) {
      otpState.failedAttemptLimitReached(utcClock)
    } else {
      otpState.loginAttemptFailed()
    }
  }
}
