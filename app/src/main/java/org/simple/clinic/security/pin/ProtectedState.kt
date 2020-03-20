package org.simple.clinic.security.pin

import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import kotlin.math.max

sealed class ProtectedState {

  companion object {
    fun from(
        state: BruteForceProtectionState,
        maxAllowedFailedAttempts: Int,
        blockAttemptsFor: Duration
    ): ProtectedState {
      val blockedAt = state.limitReachedAt
      val attemptsMade = state.failedAuthCount

      return when (blockedAt) {
        is None -> {
          val attemptsRemaining = max(0, maxAllowedFailedAttempts - attemptsMade)
          Allowed(attemptsMade = attemptsMade, attemptsRemaining = attemptsRemaining)
        }
        is Just -> Blocked(attemptsMade = attemptsMade, blockedTill = blockedAt.value + blockAttemptsFor)
      }
    }
  }

  data class Allowed(val attemptsMade: Int, val attemptsRemaining: Int) : ProtectedState()
  data class Blocked(val attemptsMade: Int, val blockedTill: Instant) : ProtectedState()
}
