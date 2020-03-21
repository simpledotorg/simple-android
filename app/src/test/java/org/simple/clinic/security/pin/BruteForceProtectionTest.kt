package org.simple.clinic.security.pin

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.InMemoryPreference
import org.simple.clinic.security.pin.ProtectedState.Allowed
import org.simple.clinic.security.pin.ProtectedState.Blocked
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.advanceTimeBy
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.threeten.bp.Duration
import org.threeten.bp.Instant

class BruteForceProtectionTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val clock = TestUtcClock()
  private val config = BruteForceProtectionConfig(limitOfFailedAttempts = 5, blockDuration = Duration.ofSeconds(0))
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

  @Test
  fun `protected state changes must be emitted correctly`() {
    setup()

    val stateChanges = bruteForceProtection.protectedStateChanges().test()

    (1..config.limitOfFailedAttempts).onEach {
      bruteForceProtection.incrementFailedAttempt().blockingAwait()
    }

    val expectedBlockedTill = Instant.now(clock) + config.blockDuration

    stateChanges.assertValues(
        Allowed(attemptsMade = 0, attemptsRemaining = 5),
        Allowed(attemptsMade = 1, attemptsRemaining = 4),
        Allowed(attemptsMade = 2, attemptsRemaining = 3),
        Allowed(attemptsMade = 3, attemptsRemaining = 2),
        Allowed(attemptsMade = 4, attemptsRemaining = 1),

        // This gets emitted because the preference gets cleared when
        // the timer for resetting the expiry runs on the current thread
        // and when it gets cleared, the preference reactive stream
        // emits the default value again.
        Allowed(attemptsMade = 0, attemptsRemaining = 5),

        Blocked(attemptsMade = 5, blockedTill = expectedBlockedTill)
    )
  }

  @Test
  fun `the locked state should be reset after the block duration passes`() {
    val blockDuration = Duration.ofSeconds(5)
    val authFailedAt = Instant.now(clock)
    val bruteForceProtectionState = BruteForceProtectionState(failedAuthCount = 5, limitReachedAt = Just(authFailedAt))
    val config = BruteForceProtectionConfig(limitOfFailedAttempts = 5, blockDuration = blockDuration)
    val schedulersProvider = TestSchedulersProvider()

    setup(
        state = bruteForceProtectionState,
        config = config,
        schedulers = schedulersProvider
    )
    val stateChanges = bruteForceProtection.protectedStateChanges().test()

    stateChanges.assertValue(Blocked(attemptsMade = 5, blockedTill = authFailedAt.plus(blockDuration)))
    assertThat(statePreference.isSet).isTrue()

    val advanceTimeBy = blockDuration.plusMillis(1)
    clock.advanceBy(advanceTimeBy)
    schedulersProvider.testScheduler.advanceTimeBy(advanceTimeBy)

    stateChanges.assertValues(
        Blocked(attemptsMade = 5, blockedTill = authFailedAt.plus(blockDuration)),
        Allowed(attemptsMade = 0, attemptsRemaining = 5)
    )
    assertThat(statePreference.isSet).isFalse()
  }

  private fun setup(
      state: BruteForceProtectionState = statePreference.defaultValue(),
      config: BruteForceProtectionConfig = this.config,
      schedulers: SchedulersProvider = TrampolineSchedulersProvider()
  ) {
    statePreference.set(state)
    bruteForceProtection = BruteForceProtection(clock, config, statePreference, schedulers)
  }
}
