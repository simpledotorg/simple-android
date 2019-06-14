package org.simple.clinic.security.pin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.security.ComparisonResult
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState
import org.simple.clinic.security.pin.PinEntryCardView.State
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import org.threeten.bp.Instant

@RunWith(JUnitParamsRunner::class)
class PinEntryCardControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<PinEntryCardView>()
  private val passwordHasher = mock<PasswordHasher>()
  private val bruteForceProtection = mock<BruteForceProtection>()

  private lateinit var controller: PinEntryCardController
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val clock = TestUtcClock()

  @Before
  fun setUp() {
    whenever(passwordHasher.compare(any(), any())).thenReturn(Single.never<ComparisonResult>())
    whenever(bruteForceProtection.incrementFailedAttempt()).thenReturn(Completable.complete())
    whenever(bruteForceProtection.recordSuccessfulAuthentication()).thenReturn(Completable.complete())
    whenever(bruteForceProtection.protectedStateChanges()).thenReturn(Observable.never())

    controller = PinEntryCardController(passwordHasher, clock, bruteForceProtection)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @After
  fun tearDown() {
    RxJavaPlugins.reset()
  }

  @Test
  fun `when 4 digits are entered then the PIN should be submitted automatically`() {
    val pinDigest = "1234"
    uiEvents.onNext(PinTextChanged("1"))
    uiEvents.onNext(PinTextChanged("12"))
    uiEvents.onNext(PinTextChanged("123"))
    uiEvents.onNext(PinTextChanged("1234"))
    uiEvents.onNext(PinDigestToVerify(pinDigest))

    verify(passwordHasher).compare(pinDigest, "1234")
  }

  @Test
  fun `when the PIN is submitted then it should be validated`() {
    val pinDigest = "1234"
    uiEvents.onNext(PinTextChanged("1234"))
    uiEvents.onNext(PinDigestToVerify(pinDigest))

    verify(passwordHasher).compare(pinDigest, "1234")
  }

  @Test
  fun `when the PIN is submitted then progress should be shown`() {
    uiEvents.onNext(PinDigestToVerify("1234"))
    uiEvents.onNext(PinTextChanged("1234"))

    verify(screen).moveToState(State.Progress)
  }

  @Test
  fun `when PIN validation fails then the progress should be hidden`() {
    val pinDigest = "1234"
    whenever(passwordHasher.compare(any(), eq(pinDigest))).thenReturn(Single.just(ComparisonResult.DIFFERENT))

    uiEvents.onNext(PinDigestToVerify(pinDigest))
    uiEvents.onNext(PinTextChanged("1234"))

    verify(screen).moveToState(PinEntryCardView.State.PinEntry)
  }

  @Test
  fun `when PIN validation fails then the PIN should be cleared`() {
    val pinDigest = "1234"
    whenever(passwordHasher.compare(any(), eq(pinDigest))).thenReturn(Single.just(ComparisonResult.DIFFERENT))

    uiEvents.onNext(PinTextChanged("1234"))
    uiEvents.onNext(PinDigestToVerify(pinDigest))

    verify(screen).clearPin()
  }

  @Test
  fun `when PIN validation succeeds then a success callback should be sent`() {
    val pinDigest = "1234"
    whenever(passwordHasher.compare(any(), eq(pinDigest))).thenReturn(Single.just(ComparisonResult.SAME))

    uiEvents.onNext(PinDigestToVerify(pinDigest))
    uiEvents.onNext(PinTextChanged("1234"))

    verify(screen).dispatchAuthenticatedCallback("1234")
  }

  @Test
  fun `when PIN validation fails then the count of failed attempts should be incremented`() {
    val pinDigest = "1234"
    whenever(passwordHasher.compare(any(), eq(pinDigest))).thenReturn(Single.just(ComparisonResult.DIFFERENT))

    uiEvents.onNext(PinDigestToVerify(pinDigest))
    uiEvents.onNext(PinTextChanged("1234"))

    verify(bruteForceProtection).incrementFailedAttempt()
  }

  @Test
  fun `when PIN validation succeeds then the count of failed attempts should be reset`() {
    val pinDigest = "1234"
    whenever(passwordHasher.compare(any(), eq(pinDigest))).thenReturn(Single.just(ComparisonResult.SAME))

    uiEvents.onNext(PinDigestToVerify(pinDigest))
    uiEvents.onNext(PinTextChanged("1234"))

    verify(bruteForceProtection).recordSuccessfulAuthentication()
  }

  @Test
  fun `when the limit of failed attempts is reached then PIN entry should remain blocked until a fixed duration`() {
    val blockedTill = Instant.now(clock) + Duration.ofMinutes(19) + Duration.ofSeconds(42)

    whenever(bruteForceProtection.protectedStateChanges())
        .thenReturn(Observable.just(
            ProtectedState.Allowed(attemptsRemaining = 2, attemptsMade = 3),
            ProtectedState.Blocked(attemptsMade = 5, blockedTill = blockedTill)))

    uiEvents.onNext(PinEntryViewCreated)

    verify(screen).moveToState(State.PinEntry)
    verify(screen).moveToState(State.BruteForceLocked(timeTillUnlock = TimerDuration(minutes = "19", seconds = "42")))
  }

  @Test
  @Parameters(value = [
    "19,42",
    "2,21",
    "0,9"
  ])
  fun `when PIN entry is blocked due to brute force then a timer should be shown to indicate remaining time`(
      minutesRemaining: Long,
      secondsRemaining: Long
  ) {
    val blockedTill = Instant.now(clock) + Duration.ofMinutes(minutesRemaining) + Duration.ofSeconds(secondsRemaining)

    whenever(bruteForceProtection.protectedStateChanges())
        .thenReturn(Observable.just(ProtectedState.Blocked(attemptsMade = 5, blockedTill = blockedTill)))

    uiEvents.onNext(PinEntryViewCreated)

    val minutesWithPadding = minutesRemaining.toString().padStart(2, padChar = '0')
    val secondsWithPadding = secondsRemaining.toString().padStart(2, padChar = '0')
    val timerDuration = TimerDuration(minutes = minutesWithPadding, seconds = secondsWithPadding)
    verify(screen).moveToState(State.BruteForceLocked(timeTillUnlock = timerDuration))
  }

  @Test
  fun `when a PIN is validated then update the error`() {
    whenever(bruteForceProtection.protectedStateChanges())
        .thenReturn(Observable.just(
            ProtectedState.Allowed(attemptsMade = 0, attemptsRemaining = 3),
            ProtectedState.Allowed(attemptsMade = 1, attemptsRemaining = 2),
            ProtectedState.Allowed(attemptsMade = 2, attemptsRemaining = 1),
            ProtectedState.Blocked(attemptsMade = 3, blockedTill = Instant.now(clock) + Duration.ofSeconds(5))
        ))

    uiEvents.onNext(PinEntryViewCreated)

    verify(screen).hideError()
    verify(screen).showIncorrectPinErrorForFirstAttempt()
    verify(screen).showIncorrectPinErrorOnSubsequentAttempts(1)
    verify(screen).showIncorrectAttemptsLimitReachedError(3)
  }

  @Test
  fun `when a PIN is submitted for verification, the current error must be cleared before the PIN verification starts`() {
    whenever(passwordHasher.compare("1234", "1234")).thenReturn(Single.just(ComparisonResult.SAME))
    val inOrder = inOrder(screen)

    uiEvents.onNext(PinDigestToVerify("1234"))
    uiEvents.onNext(PinSubmitClicked("1234"))

    inOrder.verify(screen).hideError()
    inOrder.verify(screen).moveToState(State.Progress)
    inOrder.verify(screen).dispatchAuthenticatedCallback("1234")

    inOrder.verifyNoMoreInteractions()
    verifyZeroInteractions(screen)
  }
}
