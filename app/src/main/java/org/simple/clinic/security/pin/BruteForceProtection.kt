package org.simple.clinic.security.pin

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Allowed
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Blocked
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.timer
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlin.math.max

class BruteForceProtection @Inject constructor(
    private val utcClock: UtcClock,
    private val configProvider: Observable<BruteForceProtectionConfig>,
    private val statePreference: Preference<BruteForceProtectionState>
) {

  sealed class ProtectedState {
    data class Allowed(val attemptsMade: Int, val attemptsRemaining: Int) : ProtectedState()
    data class Blocked(val attemptsMade: Int, val blockedTill: Instant) : ProtectedState()
  }

  fun incrementFailedAttempt(): Completable {
    val failedAttemptsLimitStream = configProvider.map { it.limitOfFailedAttempts }

    val pinAuthenticationFailedStream = statePreference
        .asObservable()
        .take(1)
        .map(BruteForceProtectionState::authenticationFailed)

    val updatedState = pinAuthenticationFailedStream
        .withLatestFrom(failedAttemptsLimitStream)
        .map { (failedAuthAttemptUpdatedState, maxAllowedFailedAttempts) -> updateFailedAttemptLimitReached(failedAuthAttemptUpdatedState, maxAllowedFailedAttempts) }
        .doOnNext(statePreference::set)

    return updatedState.ignoreElements()
  }

  private fun updateFailedAttemptLimitReached(
      state: BruteForceProtectionState,
      maxAllowedFailedAttempts: Int
  ): BruteForceProtectionState {
    val isLimitReached = state.failedAuthCount >= maxAllowedFailedAttempts
    return if (isLimitReached && state.limitReachedAt is None) {
      state.failedAttemptLimitReached(utcClock)
    } else {
      state
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
    val bruteForceProtectionResets = Observables
        .combineLatest(configProvider, statePreference.asObservable()) { config, state -> config.blockDuration to state.limitReachedAt }
        .switchMap { (blockDuration, blockedAt) -> signalBruteForceTimerReset(blockedAt, blockDuration) }
        .flatMapCompletable { resetFailedAttempts() }
        .toObservable<Any>()

    return Observables
        .combineLatest(
            configProvider,
            statePreference.asObservable(),
            bruteForceProtectionResets.startWith(Any())
        )
        .map { (config, state) ->
          generateProtectedState(
              blockedAt = state.limitReachedAt,
              attemptsMade = state.failedAuthCount,
              maxAllowedFailedAttempts = config.limitOfFailedAttempts,
              blockAttemptsFor = config.blockDuration
          )
        }
        .distinctUntilChanged()
  }

  private fun generateProtectedState(
      blockedAt: Optional<Instant>,
      attemptsMade: Int,
      maxAllowedFailedAttempts: Int,
      blockAttemptsFor: Duration
  ): ProtectedState {
    return when (blockedAt) {
      is None -> {
        val attemptsRemaining = max(0, maxAllowedFailedAttempts - attemptsMade)
        Allowed(attemptsMade = attemptsMade, attemptsRemaining = attemptsRemaining)
      }
      is Just -> Blocked(attemptsMade = attemptsMade, blockedTill = blockedAt.value + blockAttemptsFor)
    }
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
