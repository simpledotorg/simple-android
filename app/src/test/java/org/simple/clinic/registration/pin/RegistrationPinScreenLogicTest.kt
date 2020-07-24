package org.simple.clinic.registration.pin

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.SECURITY_PIN_LENGTH
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class RegistrationPinScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val ui = mock<RegistrationPinUi>()
  private val uiActions = mock<RegistrationPinUiActions>()

  private val ongoingRegistrationEntry = OngoingRegistrationEntry(
      uuid = UUID.fromString("db466606-9c0d-4f5f-9283-3c3c4f9b37a7"),
      fullName = "Anish Acharya"
  )

  private lateinit var testFixture: MobiusTestFixture<RegistrationPinModel, RegistrationPinEvent, RegistrationPinEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when next button is clicked then ongoing entry should be updated with the input PIN and the next screen should be opened`() {
    // given
    val input = "1234"

    // when
    setupController()
    uiEvents.onNext(RegistrationPinTextChanged(input))
    uiEvents.onNext(RegistrationPinDoneClicked())

    // then
    val entryWithPin = ongoingRegistrationEntry.withPin(input)
    verify(uiActions).openRegistrationConfirmPinScreen(entryWithPin)
    verify(ui, times(2)).hideIncompletePinError()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `proceed button clicks should only be accepted if the input PIN is of 4 digits`() {
    // given
    val validPin = "1234"
    val invalidPin = "1"

    // when
    setupController(requiredPinLength = 4)
    uiEvents.onNext(RegistrationPinTextChanged(invalidPin))
    uiEvents.onNext(RegistrationPinTextChanged(validPin))
    uiEvents.onNext(RegistrationPinDoneClicked())

    // then
    val entryWithPin = ongoingRegistrationEntry.withPin(validPin)
    verify(uiActions).openRegistrationConfirmPinScreen(entryWithPin)
    verify(ui, times(2)).hideIncompletePinError()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when proceed is clicked with a PIN of length less than 4 digits then an error should be shown`() {
    // when
    setupController(requiredPinLength = 4)
    uiEvents.onNext(RegistrationPinTextChanged("123"))
    uiEvents.onNext(RegistrationPinDoneClicked())

    // then
    verify(ui).showIncompletePinError()
    verify(uiActions, never()).openRegistrationConfirmPinScreen(any())
    verify(ui).hideIncompletePinError()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the PIN is submitted then any visible errors should be removed`() {
    // when
    setupController()
    uiEvents.onNext(RegistrationPinDoneClicked())

    // then
    verify(ui).hideIncompletePinError()
    verify(ui).showIncompletePinError()
    verifyNoMoreInteractions(ui, uiActions)
  }

  private fun setupController(
      requiredPinLength: Int = SECURITY_PIN_LENGTH,
      ongoingRegistrationEntry: OngoingRegistrationEntry = this.ongoingRegistrationEntry
  ) {
    val uiRenderer = RegistrationPinUiRenderer(ui)

    val effectHandler = RegistrationPinEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        uiActions = uiActions
    )

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = RegistrationPinModel.create(ongoingRegistrationEntry),
        init = RegistrationPinInit(),
        update = RegistrationPinUpdate(requiredPinLength = requiredPinLength),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )
    testFixture.start()
  }
}
