package org.simple.clinic.security.pin

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.threeten.bp.Duration
import org.threeten.bp.Instant

class BruteForceProtectionTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val clock = TestUtcClock()
  private val state = mock<Preference<BruteForceProtectionState>>()
  private lateinit var config: BruteForceProtectionConfig

  private lateinit var bruteForceProtection: BruteForceProtection

  @Before
  fun setup() {
    config = BruteForceProtectionConfig(isEnabled = true, limitOfFailedAttempts = 5, blockDuration = Duration.ofMinutes(20))
    bruteForceProtection = BruteForceProtection(clock, Observable.fromCallable { config }, state)
  }

  @Test
  fun `when feature flag is disabled then brute force protection should remain disabled`() {
    config = config.copy(isEnabled = false, limitOfFailedAttempts = 1)
    val initialState = BruteForceProtectionState(limitReachedAt = None)

    whenever(state.get())
        .thenReturn(initialState.copy(failedAuthCount = 0))
        .thenReturn(initialState.copy(failedAuthCount = 1))
        .thenReturn(initialState.copy(failedAuthCount = 2))

    whenever(state.asObservable())
        .thenReturn(Observable.just(
            initialState.copy(failedAuthCount = 0),
            initialState.copy(failedAuthCount = 1),
            initialState.copy(failedAuthCount = 2)))

    val stateChangesObserver = bruteForceProtection.protectedStateChanges().test()

    bruteForceProtection.incrementFailedAttempt()
        .repeat(3)
        .blockingAwait()

    stateChangesObserver
        .assertValueAt(0, ProtectedState.Allowed(attemptsMade = 0, attemptsRemaining = 1))
        .assertValueAt(1, ProtectedState.Allowed(attemptsMade = 1, attemptsRemaining = 1))
        .assertValueAt(2, ProtectedState.Allowed(attemptsMade = 1, attemptsRemaining = 1))
  }

  @Test
  fun `when incrementing the count of failed attempts then the count should correctly be updated`() {
    whenever(state.get()).thenReturn(BruteForceProtectionState(failedAuthCount = 3))

    bruteForceProtection.incrementFailedAttempt().blockingAwait()

    verify(state).set(BruteForceProtectionState(failedAuthCount = 4))
  }

  @Test
  fun `when incrementing the count of failed attempts and the limit is reached then the blocked-at time should be set`() {
    whenever(state.get()).thenReturn(BruteForceProtectionState(
        failedAuthCount = config.limitOfFailedAttempts - 1,
        limitReachedAt = None))

    bruteForceProtection.incrementFailedAttempt().blockingAwait()

    verify(state).set(BruteForceProtectionState(
        failedAuthCount = config.limitOfFailedAttempts,
        limitReachedAt = Just(Instant.now(clock))))
  }

  @Test
  fun `when incrementing the count of failed attempts and the limit was already reached then the blocked-at time should not be updated`() {
    val timeOfLastAttempt = Instant.now(clock)

    whenever(state.get()).thenReturn(BruteForceProtectionState(
        failedAuthCount = config.limitOfFailedAttempts,
        limitReachedAt = Just(timeOfLastAttempt)))

    clock.advanceBy(Duration.ofMinutes(2))
    bruteForceProtection.incrementFailedAttempt().blockingAwait()

    verify(state).set(BruteForceProtectionState(
        failedAuthCount = config.limitOfFailedAttempts + 1,
        limitReachedAt = Just(timeOfLastAttempt)))
  }

  @Test
  fun `when recording a successful login then all state should be cleared`() {
    bruteForceProtection.recordSuccessfulAuthentication().blockingAwait()

    verify(state).delete()
  }
}
