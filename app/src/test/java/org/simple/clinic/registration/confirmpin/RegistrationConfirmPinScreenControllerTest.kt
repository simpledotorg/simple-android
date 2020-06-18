package org.simple.clinic.registration.confirmpin

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import org.threeten.bp.Instant
import java.util.UUID

class RegistrationConfirmPinScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val ui = mock<RegistrationConfirmPinUi>()
  private val userSession = mock<UserSession>()
  private val clock = TestUtcClock()

  private val originalPin = "1234"
  private val ongoingEntry = OngoingRegistrationEntry(
      uuid = UUID.fromString("759f5f53-6f71-4a00-825b-c74654a5e448"),
      phoneNumber = "1111111111",
      fullName = "Anish Acharya",
      pin = originalPin
  )

  private lateinit var controllerSubscription: Disposable
  private lateinit var testFixture: MobiusTestFixture<RegistrationConfirmPinModel, RegistrationConfirmPinEvent, RegistrationConfirmPinEffect>

  @After
  fun tearDown() {
    controllerSubscription.dispose()
    testFixture.dispose()
  }

  @Test
  fun `when 4 digits are entered then the PIN should be submitted automatically`() {
    // when
    setupController()
    uiEvents.onNext(RegistrationConfirmPinTextChanged("1"))
    uiEvents.onNext(RegistrationConfirmPinTextChanged("12"))
    uiEvents.onNext(RegistrationConfirmPinTextChanged("123"))
    uiEvents.onNext(RegistrationConfirmPinTextChanged("1234"))
    uiEvents.onNext(RegistrationConfirmPinDoneClicked())

    // then
    verify(userSession).saveOngoingRegistrationEntry(ongoingEntry.withPinConfirmation("1234", Instant.now(clock)))
    verify(ui).openFacilitySelectionScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when next is clicked with a matching PIN then ongoing entry should be updated and the next screen should be opened`() {
    // given
    val input = "1234"

    // when
    setupController()
    uiEvents.onNext(RegistrationConfirmPinTextChanged(input))
    uiEvents.onNext(RegistrationConfirmPinDoneClicked())

    // then
    val inOrder = inOrder(userSession, ui)
    inOrder.verify(userSession).saveOngoingRegistrationEntry(ongoingEntry.withPinConfirmation("1234", Instant.now(clock)))
    inOrder.verify(ui).openFacilitySelectionScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `proceed button clicks should only be accepted if the confirmation pin matches with original pin`() {
    // given
    val invalidConfirmationPin = "123"
    val validConfirmationPin = "1234"

    // when
    setupController()
    uiEvents.onNext(RegistrationConfirmPinTextChanged(invalidConfirmationPin))
    uiEvents.onNext(RegistrationConfirmPinDoneClicked())

    // then
    verify(ui).showPinMismatchError()
    clearInvocations(ui)

    // when
    uiEvents.onNext(RegistrationConfirmPinTextChanged(validConfirmationPin))
    uiEvents.onNext(RegistrationConfirmPinDoneClicked())

    // then
    verify(userSession).saveOngoingRegistrationEntry(ongoingEntry.withPinConfirmation(validConfirmationPin, Instant.now(clock)))
    verify(ui).openFacilitySelectionScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when proceed is clicked with a confirmation PIN that does not match with original PIN then an error should be shown`() {
    // when
    setupController()
    uiEvents.onNext(RegistrationConfirmPinTextChanged("4567"))
    uiEvents.onNext(RegistrationConfirmPinDoneClicked())

    // then
    verify(userSession, never()).saveOngoingRegistrationEntry(any())
    verify(ui).showPinMismatchError()
    verify(ui).clearPin()
    verify(ui, never()).openFacilitySelectionScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when reset PIN is clicked then both PINs should be reset in ongoing entry and the user should be taken to the PIN entry screen`() {
    // given
    val ongoingEntryWithoutPins = ongoingEntry.copy(pin = null, pinConfirmation = null)

    // when
    setupController()
    uiEvents.onNext(RegistrationResetPinClicked())

    // then
    val inOrder = inOrder(userSession, ui)
    inOrder.verify(userSession).saveOngoingRegistrationEntry(ongoingEntryWithoutPins)
    inOrder.verify(ui).goBackToPinScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when PIN validation fails then the PIN should be cleared`() {
    // given
    val invalidConfirmationPin = "5678"

    // when
    setupController()
    uiEvents.onNext(RegistrationConfirmPinTextChanged(invalidConfirmationPin))
    uiEvents.onNext(RegistrationConfirmPinDoneClicked())

    // then
    verify(userSession, never()).saveOngoingRegistrationEntry(any())
    verify(ui).showPinMismatchError()
    verify(ui).clearPin()
    verifyNoMoreInteractions(ui)
  }

  private fun setupController(
      ongoingRegistrationEntry: OngoingRegistrationEntry = ongoingEntry
  ) {
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(ongoingRegistrationEntry.toOptional())

    val controller = RegistrationConfirmPinScreenController(userSession, clock)

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(ui) }

    val effectHandler = RegistrationConfirmPinEffectHandler(
        schedulers = TrampolineSchedulersProvider(),
        userSession = userSession,
        utcClock = clock,
        uiActions = ui
    )
    val uiRenderer = RegistrationConfirmPinUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = RegistrationConfirmPinModel.create(ongoingRegistrationEntry),
        init = RegistrationConfirmPinInit(),
        update = RegistrationConfirmPinUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )
    testFixture.start()

    uiEvents.onNext(RegistrationConfirmPinScreenCreated())
  }
}
