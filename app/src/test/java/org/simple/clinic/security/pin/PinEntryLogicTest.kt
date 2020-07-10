package org.simple.clinic.security.pin

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState
import org.simple.clinic.security.pin.PinEntryUi.Mode
import org.simple.clinic.security.pin.verification.PinVerificationMethod
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Correct
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Incorrect
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.time.Duration
import java.time.Instant

class PinEntryLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<PinEntryUi>()
  private val uiActions = mock<UiActions>()
  private val bruteForceProtection = mock<BruteForceProtection>()
  private val pinVerificationMethod = mock<PinVerificationMethod>()

  private val correctPin = "1234"
  private val incorrectPin = "1233"

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val clock = TestUtcClock()

  private val uiRenderer = PinEntryUiRenderer(ui)

  private val pinEntryEffectHandler = PinEntryEffectHandler(
      bruteForceProtection = bruteForceProtection,
      schedulersProvider = TrampolineSchedulersProvider(),
      uiActions = uiActions,
      pinVerificationMethod = pinVerificationMethod
  )

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
    testFixture.dispose()
  }

  @Test
  fun `when 4 digits are entered then the PIN should be submitted automatically`() {
    whenever(pinVerificationMethod.verify(correctPin)) doReturn Correct()
    startMobiusLoop()

    uiEvents.onNext(PinTextChanged("1"))
    uiEvents.onNext(PinTextChanged("12"))
    uiEvents.onNext(PinTextChanged("123"))
    uiEvents.onNext(PinTextChanged("1234"))

    verify(uiActions).hideError()
    verify(uiActions).setPinEntryMode(Mode.Progress)
    verify(uiActions).pinVerified(null)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when PIN validation fails then the progress should be hidden`() {
    whenever(pinVerificationMethod.verify(incorrectPin)) doReturn Incorrect()
    startMobiusLoop()

    uiEvents.onNext(PinTextChanged(incorrectPin))

    verify(uiActions).setPinEntryMode(Mode.PinEntry)
  }

  @Test
  fun `when PIN validation fails then the PIN should be cleared`() {
    whenever(pinVerificationMethod.verify(incorrectPin)) doReturn Incorrect()
    startMobiusLoop()

    uiEvents.onNext(PinTextChanged(incorrectPin))

    verify(uiActions).clearPin()
  }

  @Test
  fun `when PIN validation succeeds then a success callback should be sent`() {
    whenever(pinVerificationMethod.verify(correctPin)) doReturn Correct(correctPin)
    startMobiusLoop()

    uiEvents.onNext(PinTextChanged(correctPin))

    verify(uiActions).pinVerified(correctPin)
  }

  @Test
  fun `when PIN validation fails then the count of failed attempts should be incremented`() {
    whenever(pinVerificationMethod.verify(incorrectPin)) doReturn Incorrect()

    startMobiusLoop()

    uiEvents.onNext(PinTextChanged(incorrectPin))

    verify(bruteForceProtection).incrementFailedAttempt()
  }

  @Test
  fun `when PIN validation succeeds then the count of failed attempts should be reset`() {
    whenever(pinVerificationMethod.verify(correctPin)) doReturn Correct(correctPin)
    startMobiusLoop()

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

    startMobiusLoop()

    verify(uiActions).setPinEntryMode(Mode.PinEntry)
    verify(uiActions).setPinEntryMode(Mode.BruteForceLocked(lockUntil = blockedTill))
  }

  @Test
  fun `when PIN entry is blocked due to brute force then a timer should be shown to indicate remaining time`() {
    val minutesRemaining = 19L
    val secondsRemaining = 42L
    val blockedTill = Instant.now(clock) + Duration.ofMinutes(minutesRemaining) + Duration.ofSeconds(secondsRemaining)

    whenever(bruteForceProtection.protectedStateChanges())
        .thenReturn(Observable.just(ProtectedState.Blocked(attemptsMade = 5, blockedTill = blockedTill)))

    startMobiusLoop()

    verify(uiActions).setPinEntryMode(Mode.BruteForceLocked(lockUntil = blockedTill))
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

    startMobiusLoop()

    verify(uiActions).hideError()
    verify(uiActions).showIncorrectPinErrorForFirstAttempt()
    verify(uiActions).showIncorrectPinErrorOnSubsequentAttempts(1)
    verify(uiActions).showIncorrectAttemptsLimitReachedError(3)
  }

  @Test
  fun `when a PIN is submitted for verification, the current error must be cleared`() {
    whenever(pinVerificationMethod.verify(correctPin)) doReturn Correct(correctPin)

    startMobiusLoop()

    uiEvents.onNext(PinTextChanged(correctPin))

    verify(uiActions).hideError()
    verify(uiActions).setPinEntryMode(Mode.Progress)
    verify(uiActions).pinVerified(correctPin)
    verifyNoMoreInteractions(uiActions)
  }

  private fun startMobiusLoop() {
    testFixture.start()
  }
}
