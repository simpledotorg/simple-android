package org.simple.clinic.security.pin

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.verify
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.InMemoryPreference
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
  private val config = BruteForceProtectionConfig(limitOfFailedAttempts = 5, blockDuration = Duration.ofMinutes(20))
  private val statePreference = InMemoryPreference(
      key = "",
      defaultValue = BruteForceProtectionState(),
      actualValue = BruteForceProtectionState()
  )

  private lateinit var bruteForceProtection: BruteForceProtection

  @Test
  fun `when incrementing the count of failed attempts then the count should correctly be updated`() {
    val bruteForceProtectionState = BruteForceProtectionState(failedAuthCount = 3)

    setup(state = bruteForceProtectionState)
    bruteForceProtection.incrementFailedAttempt().blockingAwait()

    assertThat(statePreference.get()).isEqualTo(BruteForceProtectionState(failedAuthCount = 4))
  }

  @Test
  fun `when incrementing the count of failed attempts and the limit is reached then the blocked-at time should be set`() {
    val bruteForceProtectionState = BruteForceProtectionState(
        failedAuthCount = config.limitOfFailedAttempts - 1,
        limitReachedAt = None
    )

    setup(state = bruteForceProtectionState)
    bruteForceProtection.incrementFailedAttempt().blockingAwait()

    val expectedState = BruteForceProtectionState(
        failedAuthCount = config.limitOfFailedAttempts,
        limitReachedAt = Just(Instant.now(clock))
    )
    assertThat(statePreference.get()).isEqualTo(expectedState)
  }

  @Test
  fun `when incrementing the count of failed attempts and the limit was already reached then the blocked-at time should not be updated`() {
    val timeOfLastAttempt = Instant.now(clock)
    val bruteForceProtectionState = BruteForceProtectionState(
        failedAuthCount = config.limitOfFailedAttempts,
        limitReachedAt = Just(timeOfLastAttempt))

    setup(state = bruteForceProtectionState)
    clock.advanceBy(Duration.ofMinutes(2))
    bruteForceProtection.incrementFailedAttempt().blockingAwait()

    val expectedState = BruteForceProtectionState(
        failedAuthCount = config.limitOfFailedAttempts + 1,
        limitReachedAt = Just(timeOfLastAttempt)
    )
    assertThat(statePreference.get()).isEqualTo(expectedState)
  }

  @Test
  fun `when recording a successful login then all state should be cleared`() {
    setup()

    assertThat(statePreference.isSet).isTrue()
    bruteForceProtection.recordSuccessfulAuthentication().blockingAwait()

    assertThat(statePreference.isSet).isFalse()
  }

  private fun setup(
      state: BruteForceProtectionState = statePreference.defaultValue()
  ) {
    statePreference.set(state)
    bruteForceProtection = BruteForceProtection(clock, config, statePreference)
  }
}
