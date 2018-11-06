package org.simple.clinic.pin

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import org.simple.clinic.pin.BruteForceProtection.ProtectedState.Allowed
import org.simple.clinic.pin.BruteForceProtection.ProtectedState.Blocked
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class BruteForceProtection @Inject constructor(
    private val clock: Clock,
    private val configProvider: Single<BruteForceProtectionConfig>,
    @Named("pin_failed_auth_count") private val failedAuthCountPreference: Preference<Int>,    // TODO:
    @Named("pin_failed_auth_limit_reached_at") private val limitReachedAtPreference: Preference<Optional<Instant>>
) {

  companion object {

    fun defaultFailedAttemptsCount() = 0

    fun defaultAttemptsReachedAtTime(): Optional<Instant> = None
  }

  sealed class ProtectedState {
    object Allowed : ProtectedState()
    data class Blocked(val blockedTill: Instant) : ProtectedState()
  }

  fun incrementFailedAttempt(): Completable {
    return configProvider
        .flatMapCompletable { config ->
          Completable.fromAction {
            val newCount = failedAuthCountPreference.get() + 1
            failedAuthCountPreference.set(newCount)

            val isLimitReached = newCount >= config.limitOfFailedAttempts
            if (isLimitReached && limitReachedAtPreference.isSet.not()) {
              limitReachedAtPreference.set(Just(Instant.now(clock)))
            }
          }
        }
  }

  fun recordSuccessfulAuthentication(): Completable {
    return resetFailedAttempts()
  }

  private fun resetFailedAttempts(): Completable {
    return Completable.fromAction {
      failedAuthCountPreference.delete()
      limitReachedAtPreference.delete()
    }
  }

  fun protectedStateChanges(): Observable<ProtectedState> {
    val autoResets = Observables.combineLatest(configProvider.toObservable(), limitReachedAtPreference.asObservable())
        .switchMap { (config) ->
          val (blockedAt: Instant?) = limitReachedAtPreference.get()
          if (blockedAt == null) {
            Observable.empty()

          } else {
            val blockExpiresAt = blockedAt + config.blockDuration
            val now = Instant.now(clock)
            val millisTillExpiry = Math.max(blockExpiresAt.toEpochMilli() - now.toEpochMilli(), 0)
            Observable.timer(millisTillExpiry + 1, TimeUnit.MILLISECONDS)
          }
        }
        .flatMapCompletable { resetFailedAttempts() }
        .toObservable<Any>()

    return Observables.combineLatest(configProvider.toObservable(), limitReachedAtPreference.asObservable(), autoResets.startWith(Any()))
        .map { (config) ->
          val (blockedAt: Instant?) = limitReachedAtPreference.get()
          if (blockedAt == null) {
            Allowed
          } else {
            Blocked(blockedTill = blockedAt + config.blockDuration)
          }
        }
        .distinctUntilChanged()
  }
}
