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
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class RegistrationConfirmPinScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()!!
  private val screen = mock<RegistrationConfirmPinScreen>()
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

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when 4 digits are entered then the PIN should be submitted automatically`() {
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(ongoingEntry.toOptional())

    setupController()
    uiEvents.onNext(RegistrationConfirmPinTextChanged("1"))
    uiEvents.onNext(RegistrationConfirmPinTextChanged("12"))
    uiEvents.onNext(RegistrationConfirmPinTextChanged("123"))
    uiEvents.onNext(RegistrationConfirmPinTextChanged("1234"))
    uiEvents.onNext(RegistrationConfirmPinDoneClicked())

    verify(userSession).saveOngoingRegistrationEntry(ongoingEntry.withPinConfirmation("1234", clock))
    verify(screen).openFacilitySelectionScreen()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when next is clicked with a matching PIN then ongoing entry should be updated and the next screen should be opened`() {
    val input = "1234"

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(ongoingEntry.toOptional())

    setupController()
    uiEvents.onNext(RegistrationConfirmPinTextChanged(input))
    uiEvents.onNext(RegistrationConfirmPinDoneClicked())

    val inOrder = inOrder(userSession, screen)
    inOrder.verify(userSession).saveOngoingRegistrationEntry(ongoingEntry.withPinConfirmation("1234", clock))
    inOrder.verify(screen).openFacilitySelectionScreen()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `proceed button clicks should only be accepted if the confirmation pin matches with original pin`() {
    val invalidConfirmationPin = "123"
    val validConfirmationPin = "1234"

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(ongoingEntry.toOptional())

    setupController()
    uiEvents.onNext(RegistrationConfirmPinTextChanged(invalidConfirmationPin))
    uiEvents.onNext(RegistrationConfirmPinDoneClicked())

    verify(screen).showPinMismatchError()
    clearInvocations(screen)

    uiEvents.onNext(RegistrationConfirmPinTextChanged(validConfirmationPin))
    uiEvents.onNext(RegistrationConfirmPinDoneClicked())

    verify(userSession).saveOngoingRegistrationEntry(ongoingEntry.withPinConfirmation(validConfirmationPin, clock))
    verify(screen).openFacilitySelectionScreen()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when proceed is clicked with a confirmation PIN that does not match with original PIN then an error should be shown`() {
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(ongoingEntry.toOptional())

    setupController()
    uiEvents.onNext(RegistrationConfirmPinTextChanged("4567"))
    uiEvents.onNext(RegistrationConfirmPinDoneClicked())

    verify(userSession, never()).saveOngoingRegistrationEntry(any())
    verify(screen).showPinMismatchError()
    verify(screen).clearPin()
    verify(screen, never()).openFacilitySelectionScreen()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when reset PIN is clicked then both PINs should be reset in ongoing entry and the user should be taken to the PIN entry screen`() {
    val ongoingEntryWithoutPins = ongoingEntry.copy(pin = null, pinConfirmation = null)
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(ongoingEntry.toOptional())

    setupController()
    uiEvents.onNext(RegistrationResetPinClicked())

    val inOrder = inOrder(userSession, screen)
    inOrder.verify(userSession).saveOngoingRegistrationEntry(ongoingEntryWithoutPins)
    inOrder.verify(screen).goBackToPinScreen()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when PIN validation fails then the PIN should be cleared`() {
    val invalidConfirmationPin = "5678"

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(ongoingEntry.toOptional())

    setupController()
    uiEvents.onNext(RegistrationConfirmPinTextChanged(invalidConfirmationPin))
    uiEvents.onNext(RegistrationConfirmPinDoneClicked())

    verify(userSession, never()).saveOngoingRegistrationEntry(any())
    verify(screen).showPinMismatchError()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  private fun setupController() {
    val controller = RegistrationConfirmPinScreenController(userSession, clock)

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    uiEvents.onNext(RegistrationConfirmPinScreenCreated())
  }
}
