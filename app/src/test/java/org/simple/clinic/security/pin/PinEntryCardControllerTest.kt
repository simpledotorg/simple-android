package org.simple.clinic.security.pin

import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState
import org.simple.clinic.security.pin.PinEntryUi.State
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import org.threeten.bp.Duration
import org.threeten.bp.Instant

@RunWith(JUnitParamsRunner::class)
class PinEntryCardControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<PinEntryUi>()
  private val passwordHasher = JavaHashPasswordHasher()
  private val bruteForceProtection = mock<BruteForceProtection>()

  private val correctPin = "1234"
  private val incorrectPin = "1233"
  private val pinDigest = passwordHasher.hash(correctPin).blockingGet()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val clock = TestUtcClock()

  private val uiRenderer = PinEntryUiRenderer(ui)

  private val pinEntryEffectHandler = PinEntryEffectHandler(
      passwordHasher = passwordHasher,
      bruteForceProtection = bruteForceProtection,
      schedulersProvider = TrampolineSchedulersProvider(),
      uiActions = object : UiActions {
        
      }
  )

  private lateinit var controller: PinEntryCardController
  private lateinit var controllerSubscription: Disposable
  private lateinit var testFixture: MobiusTestFixture<PinEntryModel, PinEntryEvent, PinEntryEffect>

  @Before
  fun setUp() {
    whenever(bruteForceProtection.incrementFailedAttempt()).thenReturn(Completable.complete())
    whenever(bruteForceProtection.recordSuccessfulAuthentication()).thenReturn(Completable.complete())
    whenever(bruteForceProtection.protectedStateChanges()).thenReturn(Observable.never())

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = PinEntryModel.default(),
        init = PinEntryInit(),
        update = PinEntryUpdate(submitPinAtLength = 4),
        effectHandler = pinEntryEffectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  @After
  fun tearDown() {
    controllerSubscription.dispose()
    testFixture.dispose()
  }

  @Test
  fun `when 4 digits are entered then the PIN should be submitted automatically`() {
    setupController()
    startMobiusLoop()

    uiEvents.onNext(PinTextChanged("1"))
    uiEvents.onNext(PinTextChanged("12"))
    uiEvents.onNext(PinTextChanged("123"))
    uiEvents.onNext(PinTextChanged("1234"))
    uiEvents.onNext(PinDigestToVerify(pinDigest))

    verify(ui).hideError()
    verify(ui).moveToState(State.Progress)
    verify(ui).dispatchAuthenticatedCallback("1234")
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when PIN validation fails then the progress should be hidden`() {
    setupController()
    startMobiusLoop()

    uiEvents.onNext(PinDigestToVerify(pinDigest))
    uiEvents.onNext(PinTextChanged(incorrectPin))

    verify(ui).moveToState(State.PinEntry)
  }

  @Test
  fun `when PIN validation fails then the PIN should be cleared`() {
    setupController()
    startMobiusLoop()

    uiEvents.onNext(PinTextChanged(incorrectPin))
    uiEvents.onNext(PinDigestToVerify(pinDigest))

    verify(ui).clearPin()
  }

  @Test
  fun `when PIN validation succeeds then a success callback should be sent`() {
    setupController()
    startMobiusLoop()

    uiEvents.onNext(PinDigestToVerify(pinDigest))
    uiEvents.onNext(PinTextChanged(correctPin))

    verify(ui).dispatchAuthenticatedCallback(correctPin)
  }

  @Test
  fun `when PIN validation fails then the count of failed attempts should be incremented`() {
    setupController()
    startMobiusLoop()

    uiEvents.onNext(PinDigestToVerify(pinDigest))
    uiEvents.onNext(PinTextChanged(incorrectPin))

    verify(bruteForceProtection).incrementFailedAttempt()
  }

  @Test
  fun `when PIN validation succeeds then the count of failed attempts should be reset`() {
    setupController()
    startMobiusLoop()

    uiEvents.onNext(PinDigestToVerify(pinDigest))
    uiEvents.onNext(PinTextChanged(correctPin))

    verify(bruteForceProtection).recordSuccessfulAuthentication()
  }

  @Test
  fun `when the limit of failed attempts is reached then PIN entry should remain blocked until a fixed duration`() {
    val blockedTill = Instant.now(clock) + Duration.ofMinutes(19) + Duration.ofSeconds(42)

    whenever(bruteForceProtection.protectedStateChanges())
        .thenReturn(Observable.just(
            ProtectedState.Allowed(attemptsRemaining = 2, attemptsMade = 3),
            ProtectedState.Blocked(attemptsMade = 5, blockedTill = blockedTill)))

    setupController()
    startMobiusLoop()

    uiEvents.onNext(PinEntryViewCreated)

    verify(ui).moveToState(State.PinEntry)
    verify(ui).moveToState(State.BruteForceLocked(timeTillUnlock = TimerDuration(minutes = "19", seconds = "42")))
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

    setupController()
    startMobiusLoop()

    uiEvents.onNext(PinEntryViewCreated)

    val minutesWithPadding = minutesRemaining.toString().padStart(2, padChar = '0')
    val secondsWithPadding = secondsRemaining.toString().padStart(2, padChar = '0')
    val timerDuration = TimerDuration(minutes = minutesWithPadding, seconds = secondsWithPadding)
    verify(ui).moveToState(State.BruteForceLocked(timeTillUnlock = timerDuration))
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

    setupController()
    startMobiusLoop()

    uiEvents.onNext(PinEntryViewCreated)

    verify(ui).hideError()
    verify(ui).showIncorrectPinErrorForFirstAttempt()
    verify(ui).showIncorrectPinErrorOnSubsequentAttempts(1)
    verify(ui).showIncorrectAttemptsLimitReachedError(3)
  }

  @Test
  fun `when a PIN is submitted for verification, the current error must be cleared before the PIN verification starts`() {
    val inOrder = inOrder(ui)

    setupController()
    startMobiusLoop()

    uiEvents.onNext(PinDigestToVerify(pinDigest))
    uiEvents.onNext(PinSubmitClicked(correctPin))

    inOrder.verify(ui).hideError()
    inOrder.verify(ui).moveToState(State.Progress)
    inOrder.verify(ui).dispatchAuthenticatedCallback(correctPin)

    inOrder.verifyNoMoreInteractions()
    verifyZeroInteractions(ui)
  }

  private fun setupController() {
    controller = PinEntryCardController(passwordHasher, clock, bruteForceProtection)

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(ui) }
  }

  private fun startMobiusLoop() {
    testFixture.start()
  }
}
