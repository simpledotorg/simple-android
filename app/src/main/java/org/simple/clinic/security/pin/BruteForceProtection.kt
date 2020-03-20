package org.simple.clinic.security.pin

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.timer
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import javax.inject.Inject
import kotlin.math.max

class BruteForceProtection @Inject constructor(
    private val utcClock: UtcClock,
    private val config: BruteForceProtectionConfig,
    private val statePreference: Preference<BruteForceProtectionState>
) {

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

  fun incrementFailedAttempt(): Completable {
    return Completable.fromAction {
      val currentState = statePreference.get()
      val updatedState = currentState.authenticationFailed(config.limitOfFailedAttempts, utcClock)

      statePreference.set(updatedState)
    }
  }

  /** Just a proxy function, but the name makes more sense. */
  fun recordSuccessfulAuthentication(): Completable {
    return resetFailedAttempts()
  }

  fun resetFailedAttempts(): Completable {
    return Completable.fromAction { statePreference.delete() }
  }

  fun protectedStateChanges(): Observable<ProtectedState> {
    val bruteForceProtectionResets = statePreference
        .asObservable()
        .map { state -> state.limitReachedAt }
        .switchMap { blockedAt -> signalBruteForceTimerReset(blockedAt, config.blockDuration) }
        .flatMapCompletable { resetFailedAttempts() }
        .toObservable<Any>()

    return Observables
        .combineLatest(
            statePreference.asObservable(),
            bruteForceProtectionResets.startWith(Any())
        )
        .map { (state, _) ->
          ProtectedState.from(
              state = state,
              maxAllowedFailedAttempts = config.limitOfFailedAttempts,
              blockAttemptsFor = config.blockDuration
          )
        }
        .distinctUntilChanged()
  }

  private fun signalBruteForceTimerReset(
      blockedAt: Optional<Instant>,
      blockDuration: Duration
  ): Observable<Long> {
    return when (blockedAt) {
      is None -> Observable.empty()
      is Just -> {
        val resetDuration = resetBruteForceTimerIn(blockedAt.value, blockDuration)
        Observables.timer(resetDuration)
      }
    }
  }

  private fun resetBruteForceTimerIn(
      blockedAt: Instant,
      blockDuration: Duration
  ): Duration {
    val blockExpiresAt = blockedAt + blockDuration
    val now = Instant.now(utcClock)

    // It's possible that the block duration gets updated by a config update
    // from the server, potentially resulting in a situation where the expiry
    // time is now in the past.
    val millisTillExpiry = Math.max(blockExpiresAt.toEpochMilli() - now.toEpochMilli(), 0)
    return Duration.ofMillis(millisTillExpiry + 1)
  }
}
