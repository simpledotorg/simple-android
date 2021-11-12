package org.simple.clinic.security.pin

import androidx.test.annotation.UiThreadTest
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Allowed
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Blocked
import org.simple.clinic.util.Rules
import org.simple.clinic.util.TestUtcClock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class BruteForceProtectionAndroidTest {

  @Inject
  lateinit var clock: TestUtcClock

  @Inject
  lateinit var bruteForceProtection: BruteForceProtection

  @Inject
  lateinit var configProvider: Observable<BruteForceProtectionConfig>

  @Inject
  lateinit var state: Preference<BruteForceProtectionState>

  @get:Rule
  val rules: RuleChain = Rules.global()

  private val config
    get() = configProvider.blockingFirst()

  private val testScheduler = TestScheduler()

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)

    RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

    state.delete()
  }

  @After
  fun tearDown() {
    RxJavaPlugins.reset()
    state.delete()
  }

  @Test
  fun when_listening_to_protected_state_changes_then_correct_state_changes_should_be_emitted() {
    val initialState = bruteForceProtection.protectedStateChanges().blockingFirst()
    assertThat(initialState).isEqualTo(Allowed(attemptsMade = 0, attemptsRemaining = config.limitOfFailedAttempts))

    bruteForceProtection.incrementFailedAttempt()
        .repeat(config.limitOfFailedAttempts.toLong())
        .blockingAwait()

    val stateAfterLimitReached = bruteForceProtection.protectedStateChanges().blockingFirst()
    val expectedBlockedTill = Instant.now(clock) + config.blockDuration
    assertThat(stateAfterLimitReached).isEqualTo(Blocked(blockedTill = expectedBlockedTill, attemptsMade = config.limitOfFailedAttempts))
  }

  /**
   * This test runs on the Ui thread because SharedPreferences implicitly uses the Ui thread
   * for delivering change notifications. Running a test with multi-threading is difficult
   * otherwise.
   */
  @Test
  @UiThreadTest
  fun should_unlock_after_block_duration() {
    val attemptsRemainingOnStart = 1
    val attemptsMadeOnStart = config.limitOfFailedAttempts - attemptsRemainingOnStart

    state.set(BruteForceProtectionState(failedAuthCount = attemptsMadeOnStart))

    val stateChangeObserver = bruteForceProtection.protectedStateChanges().test()

    // Initial state.
    stateChangeObserver.assertValues(Allowed(
        attemptsMade = attemptsMadeOnStart,
        attemptsRemaining = attemptsRemainingOnStart))

    bruteForceProtection.incrementFailedAttempt().blockingAwait()
    stateChangeObserver.assertValueAt(1, Blocked(
        attemptsMade = attemptsMadeOnStart + 1,
        blockedTill = Instant.now(clock) + config.blockDuration))

    val advanceTimeByMillis = { millis: Long ->
      clock.advanceBy(Duration.ofMillis(millis))
      testScheduler.advanceTimeBy(millis, TimeUnit.MILLISECONDS)
    }

    advanceTimeByMillis(config.blockDuration.toMillis())
    stateChangeObserver.assertValueCount(2)

    advanceTimeByMillis(1)
    stateChangeObserver.assertValueAt(2, Allowed(attemptsMade = 0, attemptsRemaining = config.limitOfFailedAttempts))
    stateChangeObserver.assertValueCount(3)

    stateChangeObserver.dispose()
  }
}
