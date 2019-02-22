package org.simple.clinic.security.pin

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Allowed
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Blocked
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.timer
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import javax.inject.Inject

class BruteForceProtection @Inject constructor(
    private val utcClock: UtcClock,
    private val configProvider: Single<BruteForceProtectionConfig>,
    private val statePreference: Preference<BruteForceProtectionState>
) {

  sealed class ProtectedState {
    data class Allowed(val attemptsMade: Int, val attemptsRemaining: Int) : ProtectedState()
    data class Blocked(val attemptsMade: Int, val blockedTill: Instant) : ProtectedState()
  }

  fun incrementFailedAttempt(): Completable {
    return configProvider
        .flatMapCompletable { config ->
          Completable.fromAction {
            val state = statePreference.get()
            val newFailedAuthCount = state.failedAuthCount + 1

            var updatedState = state.copy(failedAuthCount = newFailedAuthCount)

            val isLimitReached = newFailedAuthCount >= config.limitOfFailedAttempts
            if (isLimitReached && state.limitReachedAt is None) {
              updatedState = updatedState.copy(limitReachedAt = Just(Instant.now(utcClock)))
            }

            statePreference.set(updatedState)
          }
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
    val autoResets = Observables.combineLatest(configProvider.toObservable(), statePreference.asObservable())
        .switchMap { (config) ->
          val state = statePreference.get()
          val (blockedAt: Instant?) = state.limitReachedAt

          if (blockedAt == null) {
            Observable.empty()

          } else {
            val blockExpiresAt = blockedAt + config.blockDuration
            val now = Instant.now(utcClock)

            // It's possible that the block duration gets updated by a config update
            // from the server, potentially resulting in a situation where the expiry
            // time is now in the past.
            val millisTillExpiry = Math.max(blockExpiresAt.toEpochMilli() - now.toEpochMilli(), 0)
            val resetIn = Duration.ofMillis(millisTillExpiry + 1)
            Observables.timer(resetIn)
          }
        }
        .flatMapCompletable { resetFailedAttempts() }
        .toObservable<Any>()

    val alwaysAllowWhenDisabled = Observables
        .combineLatest(configProvider.toObservable(), statePreference.asObservable())
        .filter { (config) -> config.isEnabled.not() }
        .map { (_, state) -> Allowed(attemptsMade = Math.min(1, state.failedAuthCount), attemptsRemaining = 1) }

    return Observables.combineLatest(configProvider.toObservable(), statePreference.asObservable(), autoResets.startWith(Any()))
        .filter { (config) -> config.isEnabled }
        .map { (config, state) ->
          val (blockedAt: Instant?) = state.limitReachedAt
          val attemptsMade = state.failedAuthCount

          if (blockedAt == null) {
            val attemptsRemaining = Math.max(0, config.limitOfFailedAttempts - attemptsMade)
            Allowed(attemptsMade = attemptsMade, attemptsRemaining = attemptsRemaining)

          } else {
            Blocked(attemptsMade = attemptsMade, blockedTill = blockedAt + config.blockDuration)
          }
        }
        .distinctUntilChanged()
        .mergeWith(alwaysAllowWhenDisabled)
  }
}
