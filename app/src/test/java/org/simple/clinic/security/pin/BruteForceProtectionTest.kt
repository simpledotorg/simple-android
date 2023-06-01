package org.simple.clinic.security.pin

import com.f2prateek.rx.preferences2.Preference
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import io.reactivex.Observable
import org.junit.Rule
import org.junit.Test
import org.simple.sharedTestCode.util.RxErrorsRule
import org.simple.sharedTestCode.util.TestUtcClock
import java.time.Duration
import java.time.Instant
import java.util.Optional

class BruteForceProtectionTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val clock = TestUtcClock()
  private val state = mock<Preference<BruteForceProtectionState>>()
  private val config = BruteForceProtectionConfig(limitOfFailedAttempts = 5, blockDuration = Duration.ofMinutes(20))

  private val bruteForceProtection = BruteForceProtection(clock, Observable.just(config), state)

  @Test
  fun `when incrementing the count of failed attempts then the count should correctly be updated`() {
    val bruteForceProtectionState = BruteForceProtectionState(failedAuthCount = 3)
    whenever(state.asObservable()).thenReturn(Observable.just(bruteForceProtectionState))

    bruteForceProtection.incrementFailedAttempt().blockingAwait()

    verify(state).set(BruteForceProtectionState(failedAuthCount = 4))
  }

  @Test
  fun `when incrementing the count of failed attempts and the limit is reached then the blocked-at time should be set`() {
    val bruteForceProtectionState = BruteForceProtectionState(
        failedAuthCount = config.limitOfFailedAttempts - 1,
        limitReachedAt = Optional.empty()
    )
    whenever(state.asObservable()).thenReturn(Observable.just(bruteForceProtectionState))

    bruteForceProtection.incrementFailedAttempt().blockingAwait()

    verify(state).set(BruteForceProtectionState(
        failedAuthCount = config.limitOfFailedAttempts,
        limitReachedAt = Optional.of(Instant.now(clock))))
  }

  @Test
  fun `when incrementing the count of failed attempts and the limit was already reached then the blocked-at time should not be updated`() {
    val timeOfLastAttempt = Instant.now(clock)
    val bruteForceProtectionState = BruteForceProtectionState(
        failedAuthCount = config.limitOfFailedAttempts,
        limitReachedAt = Optional.of(timeOfLastAttempt))
    whenever(state.asObservable()).thenReturn(Observable.just(bruteForceProtectionState))

    clock.advanceBy(Duration.ofMinutes(2))
    bruteForceProtection.incrementFailedAttempt().blockingAwait()

    verify(state).set(BruteForceProtectionState(
        failedAuthCount = config.limitOfFailedAttempts + 1,
        limitReachedAt = Optional.of(timeOfLastAttempt)))
  }

  @Test
  fun `when recording a successful login then all state should be cleared`() {
    bruteForceProtection.recordSuccessfulAuthentication().blockingAwait()

    verify(state).delete()
  }
}
