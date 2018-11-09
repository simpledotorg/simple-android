package org.simple.clinic.security.pin

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.TestClock
import org.threeten.bp.Duration
import org.threeten.bp.Instant

class BruteForceProtectionTest {

  private val failedPinAuthCount = mock<Preference<Int>>()
  private val limitReachedAt = mock<Preference<Optional<Instant>>>()
  private val clock = TestClock()
  private lateinit var config: BruteForceProtectionConfig

  private lateinit var bruteForceProtection: BruteForceProtection

  @Before
  fun setup() {
    config = BruteForceProtectionConfig(isEnabled = true, limitOfFailedAttempts = 5, blockDuration = Duration.ofMinutes(20))
    bruteForceProtection = BruteForceProtection(clock, Single.fromCallable { config }, failedPinAuthCount, limitReachedAt)
  }

  @Test
  fun `when feature flag is disabled then brute force protection should remain disabled`() {
    whenever(failedPinAuthCount.get()).thenReturn(0).thenReturn(1).thenReturn(2)
    whenever(failedPinAuthCount.asObservable()).thenReturn(Observable.just(0, 1, 2))

    whenever(limitReachedAt.get()).thenReturn(None).thenReturn(None).thenReturn(None)
    whenever(limitReachedAt.asObservable()).thenReturn(Observable.just(None, None, None))

    config = config.copy(isEnabled = false)

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
    whenever(failedPinAuthCount.get()).thenReturn(3)

    bruteForceProtection.incrementFailedAttempt().blockingAwait()

    verify(failedPinAuthCount).set(4)
  }

  @Test
  fun `when incrementing the count of failed attempts and the limit is reached then the blocked-at time should be set`() {
    whenever(failedPinAuthCount.get()).thenReturn(config.limitOfFailedAttempts - 1)
    whenever(limitReachedAt.get()).thenReturn(None)

    bruteForceProtection.incrementFailedAttempt().blockingAwait()

    verify(failedPinAuthCount).set(config.limitOfFailedAttempts)
    verify(limitReachedAt).set(Just(Instant.now(clock)))
  }

  @Test
  fun `when incrementing the count of failed attempts and the limit was already reached then the blocked-at time should not be updated`() {
    whenever(failedPinAuthCount.get()).thenReturn(config.limitOfFailedAttempts)
    whenever(limitReachedAt.isSet).thenReturn(true)

    bruteForceProtection.incrementFailedAttempt().blockingAwait()

    verify(failedPinAuthCount).set(config.limitOfFailedAttempts + 1)
    verify(limitReachedAt, never()).set(any())
  }

  @Test
  fun `when recording a successful login then all state should be cleared`() {
    bruteForceProtection.recordSuccessfulAuthentication().blockingAwait()

    verify(limitReachedAt).delete()
    verify(failedPinAuthCount).delete()
  }
}
