package org.simple.clinic.enterotp

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.timer
import java.time.Duration
import java.time.Instant
import java.util.Optional
import javax.inject.Inject
import kotlin.math.max

class BruteForceOtpEntryProtection @Inject constructor(
    private val utcClock: UtcClock,
    private val config: BruteForceOtpEntryProtectionConfig,
    private val otpStatePreference: Preference<BruteForceOtpProtectionState>
) {

  sealed class ProtectedState {
    data class Allowed(val attemptsMade: Int, val attemptsRemaining: Int) : ProtectedState()
    data class Blocked(val attemptsMade: Int, val blockedTill: Instant) : ProtectedState()
  }

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

  fun protectedStateChanges(): Observable<ProtectedState> {
    val bruteForceProtectionResets = Observables
        .combineLatest(config, otpStatePreference.asObservable()) { config, state -> config.blockDuration to state.limitReachedAt }
        .switchMap { (blockDuration, blockedAt) -> signalBruteForceTimerReset(blockedAt, blockDuration) }
        .flatMapCompletable { resetFailedAttempts() }
        .toObservable<Any>()

    return Observables
        .combineLatest(
            config,
            otpStatePreference.asObservable(),
            bruteForceProtectionResets.startWith(Any())
        )
        .map { (config, state) ->
          generateProtectedState(
              blockedAt = state.limitReachedAt,
              attemptsMade = state.failedLoginOtpAttempt,
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
    return blockedAt
        .map { ProtectedState.Blocked(attemptsMade = attemptsMade, blockedTill = it + blockAttemptsFor) as ProtectedState }
        .orElseGet {
          val attemptsRemaining = max(0, maxAllowedFailedAttempts - attemptsMade)
          ProtectedState.Allowed(attemptsMade = attemptsMade, attemptsRemaining = attemptsRemaining)
        }
  }

  private fun signalBruteForceTimerReset(
      blockedAt: Optional<Instant>,
      blockDuration: Duration
  ): Observable<Long> {
    return blockedAt
        .map {
          val resetDuration = resetBruteForceTimerIn(it, blockDuration)
          Observables.timer(resetDuration)
        }
        .orElse(Observable.empty())
  }

  private fun resetBruteForceTimerIn(
      blockedAt: Instant,
      blockDuration: Duration
  ): Duration {
    val blockExpiresAt = blockedAt + blockDuration
    val now = Instant.now(utcClock)

    val millisTillExpiry = (blockExpiresAt.toEpochMilli() - now.toEpochMilli()).coerceAtLeast(0)
    return Duration.ofMillis(millisTillExpiry + 1)
  }

  private fun resetFailedAttempts(): Completable {
    return Completable.fromAction { otpStatePreference.delete() }
  }
}
