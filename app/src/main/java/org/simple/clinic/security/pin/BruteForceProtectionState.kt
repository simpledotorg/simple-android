package org.simple.clinic.security.pin

import com.squareup.moshi.JsonClass
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class BruteForceProtectionState(
    val failedAuthCount: Int = 0,
    val limitReachedAt: Optional<Instant> = None
) {

  fun authenticationFailed(
      maxAllowedFailedAttempts: Int,
      clock: UtcClock
  ): BruteForceProtectionState {
    val totalFailedAttempts = failedAuthCount + 1
    val isLimitReached = totalFailedAttempts >= maxAllowedFailedAttempts
    val limitReachedAtTimestamp = if (isLimitReached && limitReachedAt is None) Just(Instant.now(clock)) else limitReachedAt

    return copy(
        failedAuthCount = totalFailedAttempts,
        limitReachedAt = limitReachedAtTimestamp
    )
  }
}
