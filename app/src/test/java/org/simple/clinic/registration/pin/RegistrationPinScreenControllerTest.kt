package org.simple.clinic.registration.pin

import com.nhaarman.mockito_kotlin.mock
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
  fun `when screen is created then an empty ongoing entry should be created`() {
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry())).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationPinScreenCreated())

    verify(userSession).saveOngoingRegistrationEntry(OngoingRegistrationEntry())
  }

  @Test
  fun `when next button is clicked then ongoing entry should be updated with the input pin and the next screen should be opened`() {
    val input = "1234"

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(pin = input))).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationPinTextChanged(input))
    uiEvents.onNext(RegistrationPinNextClicked())

    verify(userSession).saveOngoingRegistrationEntry(OngoingRegistrationEntry(pin = input))
    verify(screen).openRegistrationConfirmPinScreen()
  }

  @Test
  fun `while pin field is empty then the next button should remain disabled`() {
    uiEvents.onNext(RegistrationPinTextChanged(""))
    uiEvents.onNext(RegistrationPinTextChanged("1"))
    uiEvents.onNext(RegistrationPinTextChanged("12"))
    uiEvents.onNext(RegistrationPinTextChanged(""))

    verify(screen, times(2)).setNextButtonEnabled(false)
    verify(screen, times(1)).setNextButtonEnabled(true)
  }
}
