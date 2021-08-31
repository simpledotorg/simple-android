package org.simple.clinic.enterotp

import com.squareup.moshi.JsonClass
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.toOptional
import java.time.Instant
import java.util.Optional

@JsonClass(generateAdapter = true)
data class BruteForceOtpProtectionState(
    val limitReachedAt: Optional<Instant> = Optional.empty(),
    val failedLoginOtpAttempt: Int = 0
) {

  fun failedAttemptLimitReached(utcClock: UtcClock): BruteForceOtpProtectionState {
    return this.copy(limitReachedAt = Instant.now(utcClock).toOptional())
  }

  fun loginAttemptFailed(): BruteForceOtpProtectionState {
    return this.copy(failedLoginOtpAttempt = failedLoginOtpAttempt + 1)
  }
}
