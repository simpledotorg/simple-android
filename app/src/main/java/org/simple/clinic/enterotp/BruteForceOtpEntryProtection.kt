package org.simple.clinic.enterotp

import android.os.Parcelable
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.timer
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class BruteForceOtpEntryProtection @Inject constructor(
    private val utcClock: UtcClock,
    private val config: BruteForceOtpEntryProtectionConfig,
    private val otpStatePreference: Preference<BruteForceOtpProtectionState>
) {

  sealed class ProtectedState : Parcelable {
    @Parcelize
    data class Allowed(val attemptsMade: Int, val attemptsRemaining: Int) : ProtectedState()

    @Parcelize
    data class Blocked(val attemptsMade: Int, val blockedTill: Instant) : ProtectedState()
  }

  fun incrementFailedOtpAttempt() {
    val updatedBruteForceOtpProtectionState = updateFailedOtpAttemptLimitReached()
    otpStatePreference.set(updatedBruteForceOtpProtectionState)
  }

  private fun updateFailedOtpAttemptLimitReached(): BruteForceOtpProtectionState {
    val otpState = otpStatePreference.get()
    val updatedOtpState = otpState.loginAttemptFailed()
    val isOtpAttemptLimitReached = config.limitOfFailedAttempts == updatedOtpState.failedLoginOtpAttempt
    return if (isOtpAttemptLimitReached && !otpState.limitReachedAt.isPresent) {
      updatedOtpState.failedAttemptLimitReached(utcClock)
    } else if (!isOtpAttemptLimitReached && otpState.limitReachedAt.isPresent) {
      otpState
    } else
      updatedOtpState
  }

  fun protectedStateChanges(): Observable<ProtectedState> {
    val bruteForceProtectionReset = otpStatePreference
        .asObservable()
        .switchMap(::signalBruteForceTimerReset)
        .doOnNext { resetFailedAttempts() }
        .startWith(Unit)

    return Observables
        .combineLatest(
            otpStatePreference.asObservable(),
            bruteForceProtectionReset
        )
        .map { (state, _) -> generateProtectedState(state) }
        .distinctUntilChanged()
  }

  private fun generateProtectedState(state: BruteForceOtpProtectionState): ProtectedState {
    val blockedAt = state.limitReachedAt
    return blockedAt
        .map<ProtectedState> {
          val blockedTill = it + config.blockDuration
          ProtectedState.Blocked(attemptsMade = state.failedLoginOtpAttempt, blockedTill = blockedTill)
        }
        .orElseGet {
          val attemptsRemaining = config.limitOfFailedAttempts - state.failedLoginOtpAttempt
          ProtectedState.Allowed(attemptsMade = state.failedLoginOtpAttempt, attemptsRemaining = attemptsRemaining)
        }
  }

  private fun signalBruteForceTimerReset(
      state: BruteForceOtpProtectionState
  ): Observable<Unit> {
    return state
        .limitReachedAt
        .map {
          val resetDuration = resetBruteForceTimerIn(it, config.blockDuration)
          Observables
              .timer(resetDuration)
              .map { Unit }
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

  fun resetFailedAttempts() {
    otpStatePreference.delete()
  }
}
