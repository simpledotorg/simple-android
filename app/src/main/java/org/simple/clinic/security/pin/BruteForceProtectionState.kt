package org.simple.clinic.security.pin

import com.squareup.moshi.JsonClass
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.toOptional
import java.time.Instant
import java.util.Optional

@JsonClass(generateAdapter = true)
data class BruteForceProtectionState(
    val failedAuthCount: Int = 0,
    val limitReachedAt: Optional<Instant> = Optional.empty()
) {

  fun authenticationFailed(): BruteForceProtectionState {
    return this.copy(failedAuthCount = failedAuthCount + 1)
  }

  fun failedAttemptLimitReached(utcClock: UtcClock): BruteForceProtectionState {
    return this.copy(limitReachedAt = Instant.now(utcClock).toOptional())
  }
}
