package org.simple.clinic.registration.confirmpin

import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class RegistrationConfirmPinLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val ui = mock<RegistrationConfirmPinUi>()
  private val uiActions = mock<RegistrationConfirmPinUiActions>()

  private val originalPin = "1234"
  private val ongoingEntry = OngoingRegistrationEntry(
      uuid = UUID.fromString("759f5f53-6f71-4a00-825b-c74654a5e448"),
      phoneNumber = "1111111111",
      fullName = "Anish Acharya",
      pin = originalPin
  )

  private lateinit var testFixture: MobiusTestFixture<RegistrationConfirmPinModel, RegistrationConfirmPinEvent, RegistrationConfirmPinEffect>

  @After
  fun tearDown() {
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
    verify(uiActions).openFacilitySelectionScreen(ongoingEntry)
    verifyNoMoreInteractions(ui, uiActions)
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
    verify(uiActions).openFacilitySelectionScreen(ongoingEntry)
    verifyNoMoreInteractions(ui, uiActions)
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
    clearInvocations(ui, uiActions)

    // when
    uiEvents.onNext(RegistrationConfirmPinTextChanged(validConfirmationPin))
    uiEvents.onNext(RegistrationConfirmPinDoneClicked())

    // then
    verify(uiActions).openFacilitySelectionScreen(ongoingEntry)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when proceed is clicked with a confirmation PIN that does not match with original PIN then an error should be shown`() {
    // when
    setupController()
    uiEvents.onNext(RegistrationConfirmPinTextChanged("4567"))
    uiEvents.onNext(RegistrationConfirmPinDoneClicked())

    // then
    verify(ui).showPinMismatchError()
    verify(uiActions).clearPin()
    verify(uiActions, never()).openFacilitySelectionScreen(ongoingEntry)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when reset PIN is clicked then both PINs should be reset in ongoing entry and the user should be taken to the PIN entry screen`() {
    // given
    val ongoingEntryWithoutPins = ongoingEntry.copy(pin = null)

    // when
    setupController()
    uiEvents.onNext(RegistrationResetPinClicked())

    // then
    verify(uiActions).goBackToPinScreen(ongoingEntryWithoutPins)
    verifyNoMoreInteractions(ui, uiActions)
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
    verify(ui).showPinMismatchError()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  private fun setupController(
      ongoingRegistrationEntry: OngoingRegistrationEntry = ongoingEntry
  ) {
    val effectHandler = RegistrationConfirmPinEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        uiActions = uiActions
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
  }
}
