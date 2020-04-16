package org.simple.clinic.registration.pin

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent

class RegistrationPinScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

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
  fun `when 4 digits are entered then the PIN should be submitted automatically`() {
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationPinTextChanged("1"))
    uiEvents.onNext(RegistrationPinTextChanged("12"))
    uiEvents.onNext(RegistrationPinTextChanged("123"))
    uiEvents.onNext(RegistrationPinTextChanged("1234"))

    verify(userSession).saveOngoingRegistrationEntry(OngoingRegistrationEntry(pin = "1234"))
  }

  @Test
  fun `when next button is clicked then ongoing entry should be updated with the input PIN and the next screen should be opened`() {
    val input = "1234"

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(pin = input))).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationPinTextChanged(input))

    verify(userSession).saveOngoingRegistrationEntry(OngoingRegistrationEntry(pin = input))
    verify(screen).openRegistrationConfirmPinScreen()
  }

  @Test
  fun `proceed button clicks should only be accepted if the input PIN is of 4 digits`() {
    val validPin = "1234"
    val invalidPin = "1"

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(pin = validPin))).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationPinTextChanged(invalidPin))
    uiEvents.onNext(RegistrationPinTextChanged(validPin))

    verify(userSession, times(1)).saveOngoingRegistrationEntry(any())
    verify(screen, times(1)).openRegistrationConfirmPinScreen()
  }

  @Test
  fun `when proceed is clicked with a PIN of length less than 4 digits then an error should be shown`() {
    uiEvents.onNext(RegistrationPinTextChanged("123"))
    uiEvents.onNext(RegistrationPinDoneClicked())

    verify(screen).showIncompletePinError()
    verify(userSession, never()).saveOngoingRegistrationEntry(any())
    verify(screen, never()).openRegistrationConfirmPinScreen()
  }

  @Test
  fun `when the PIN is submitted then any visible errors should be removed`() {
    uiEvents.onNext(RegistrationPinDoneClicked())
    verify(screen).hideIncompletePinError()
  }
}
