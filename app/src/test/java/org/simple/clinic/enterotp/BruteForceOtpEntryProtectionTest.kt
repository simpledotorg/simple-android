package org.simple.clinic.enterotp

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import java.time.Duration

class BruteForceOtpEntryProtectionTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val preferenceState = mock<Preference<BruteForceOtpProtectionState>>()
  private val clock = TestUtcClock()
  private val config = BruteForceOtpEntryProtectionConfig(limitOfFailedAttempts = 4, blockDuration = Duration.ofMinutes(20))

  private val bruteForceOtpEntryProtection = BruteForceOtpEntryProtection(clock, config, preferenceState)

  @Test
  fun `when incrementing the count of failed otp attempts, then the count should be updated`() {
    val state = BruteForceOtpProtectionState(failedLoginOtpAttempt = 2)
    whenever(preferenceState.get()).thenReturn(state)

    bruteForceOtpEntryProtection.incrementFailedOtpAttempt()

    verify(preferenceState).set(BruteForceOtpProtectionState(failedLoginOtpAttempt = 3))
  }
}
