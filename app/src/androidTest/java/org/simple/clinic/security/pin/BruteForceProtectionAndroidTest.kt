package org.simple.clinic.security.pin

import android.support.test.annotation.UiThreadTest
import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Allowed
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Blocked
import org.simple.clinic.util.Optional
import org.simple.clinic.util.TestClock
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class BruteForceProtectionAndroidTest {

  @Inject
  lateinit var clock: Clock

  @Inject
  lateinit var bruteForceProtection: BruteForceProtection

  @Inject
  lateinit var configProvider: Single<BruteForceProtectionConfig>

  @Inject
  @field:Named("pin_failed_auth_count")
  lateinit var failedAttemptsCount: Preference<Int>

  @Inject
  @field:Named("pin_failed_auth_limit_reached_at")
  lateinit var blockedAt: Preference<Optional<Instant>>

  private val testClock
    get() = clock as TestClock

  private val config
    get() = configProvider.blockingGet()

  private val testScheduler = TestScheduler()

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)

    RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

    failedAttemptsCount.delete()
    blockedAt.delete()
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
    failedAttemptsCount.set(attemptsMadeOnStart)

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
      testClock.advanceBy(Duration.ofMillis(millis))
      testScheduler.advanceTimeBy(millis, TimeUnit.MILLISECONDS)
    }

    advanceTimeByMillis(config.blockDuration.toMillis())
    stateChangeObserver.assertValueCount(2)

    advanceTimeByMillis(1)
    stateChangeObserver.assertValueAt(2, Allowed(attemptsMade = 0, attemptsRemaining = config.limitOfFailedAttempts))
    stateChangeObserver.assertValueCount(3)
  }

  @After
  fun tearDown() {
    RxJavaPlugins.reset()
    testClock.resetToEpoch()
    failedAttemptsCount.delete()
    blockedAt.delete()
  }
}
