package org.simple.clinic.enterotp

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Rule
import org.junit.Test
import org.simple.sharedTestCode.util.RxErrorsRule
import org.simple.sharedTestCode.util.TestUtcClock
import java.time.Duration
import java.time.Instant
import java.util.Optional

class BruteForceOtpEntryProtectionTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val preferenceState = mock<Preference<BruteForceOtpProtectionState>>()
  private val clock = TestUtcClock()
  private val config = BruteForceOtpEntryProtectionConfig(limitOfFailedAttempts = 5, blockDuration = Duration.ofMinutes(20), minOtpEntries = 3)

  private val bruteForceOtpEntryProtection = BruteForceOtpEntryProtection(clock, config, preferenceState)

  @Test
  fun `when incrementing the count of failed otp attempts, then the count should be updated`() {
    val state = BruteForceOtpProtectionState(failedLoginOtpAttempt = 2)
    preferenceState.set(state)
    whenever(preferenceState.get()).thenReturn(state)

    bruteForceOtpEntryProtection.incrementFailedOtpAttempt()

    verify(preferenceState).set(BruteForceOtpProtectionState(failedLoginOtpAttempt = 3))
  }

  @Test
  fun `when incrementing the count of failed attempts and the limit has reached then the blocked-at time should be set`() {
    val bruteForceOtpProtectionState = BruteForceOtpProtectionState(
        limitReachedAt = Optional.empty(),
        failedLoginOtpAttempt = config.limitOfFailedAttempts - 1
    )
    whenever(preferenceState.get()).thenReturn(bruteForceOtpProtectionState)

    bruteForceOtpProtectionState.loginAttemptFailed()
    bruteForceOtpEntryProtection.incrementFailedOtpAttempt()

    verify(preferenceState).set(BruteForceOtpProtectionState(
        limitReachedAt = Optional.of(Instant.now(clock)),
        failedLoginOtpAttempt = config.limitOfFailedAttempts
    ))
  }

  @Test
  fun `when otp limit was already reached then the blocked-at time and the number of attempts should not be updated`() {
    val timeOfLastAttempt = Instant.now(clock)
    val bruteForceOtpProtectionState = BruteForceOtpProtectionState(
        failedLoginOtpAttempt = config.limitOfFailedAttempts,
        limitReachedAt = Optional.of(timeOfLastAttempt))
    whenever(preferenceState.get()).thenReturn(bruteForceOtpProtectionState)

    clock.advanceBy(Duration.ofMinutes(2))
    bruteForceOtpEntryProtection.incrementFailedOtpAttempt()

    verify(preferenceState).set(BruteForceOtpProtectionState(
        failedLoginOtpAttempt = config.limitOfFailedAttempts,
        limitReachedAt = Optional.of(timeOfLastAttempt)))
  }
}
