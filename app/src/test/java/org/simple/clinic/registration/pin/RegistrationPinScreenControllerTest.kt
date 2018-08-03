package org.simple.clinic.registration.pin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent

class RegistrationPinScreenControllerTest {

  val uiEvents = PublishSubject.create<UiEvent>()!!
  val screen = mock<RegistrationPinScreen>()
  val userSession = mock<UserSession>()

  private lateinit var controller: RegistrationPinScreenController

  @Before
  fun setUp() {
    controller = RegistrationPinScreenController(userSession)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when next button is clicked then ongoing entry should be updated with the input pin and the next screen should be opened`() {
    val input = "1234"

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(pin = input))).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationPinTextChanged(input))
    uiEvents.onNext(RegistrationPinDoneClicked())

    verify(userSession).saveOngoingRegistrationEntry(OngoingRegistrationEntry(pin = input))
    verify(screen).openRegistrationConfirmPinScreen()
  }

  @Test
  fun `when screen is created then user's existing details should be pre-filled`() {
    val ongoingEntry = OngoingRegistrationEntry(
        fullName = "Ashok Kumar",
        phoneNumber = "1234567890",
        pin = "1234")
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(ongoingEntry))

    uiEvents.onNext(RegistrationPinScreenCreated())

    verify(screen).preFillUserDetails(ongoingEntry)
  }

  @Test
  fun `proceed button clicks should only be accepted if the input pin is of 4 digits`() {
    val validPin = "1234"
    val invalidPin = "1"

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(pin = validPin))).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationPinTextChanged(invalidPin))
    uiEvents.onNext(RegistrationPinDoneClicked())

    uiEvents.onNext(RegistrationPinTextChanged(validPin))
    uiEvents.onNext(RegistrationPinDoneClicked())

    verify(userSession, times(1)).saveOngoingRegistrationEntry(any())
    verify(screen, times(1)).openRegistrationConfirmPinScreen()
  }

  @Test
  fun `when proceed is clicked with a pin of length less than 4 digits then an error should be shown`() {
    uiEvents.onNext(RegistrationPinTextChanged("123"))
    uiEvents.onNext(RegistrationPinDoneClicked())

    verify(screen).showIncompletePinError()
    verify(userSession, never()).saveOngoingRegistrationEntry(any())
    verify(screen, never()).openRegistrationConfirmPinScreen()
  }

  @Test
  fun `when input pin is changed then any visible errors should be removed`() {
    uiEvents.onNext(RegistrationPinTextChanged(""))
    verify(screen).hideIncompletePinError()
  }
}
