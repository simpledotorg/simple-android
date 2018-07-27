package org.simple.clinic.registration.confirmpin

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.registration.RegistrationScheduler
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent

class RegistrationConfirmPinScreenControllerTest {

  val uiEvents = PublishSubject.create<UiEvent>()!!
  val screen = mock<RegistrationConfirmPinScreen>()
  val userSession = mock<UserSession>()
  val scheduler = mock<RegistrationScheduler>()

  private lateinit var controller: RegistrationConfirmPinScreenController

  @Before
  fun setUp() {
    controller = RegistrationConfirmPinScreenController(userSession, scheduler)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when next button is clicked then ongoing entry should be updated with the input pin and the next screen should be opened`() {
    val input = "1234"

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(pinConfirmation = input))).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationConfirmPinTextChanged(input))
    uiEvents.onNext(RegistrationConfirmPinNextClicked())

    verify(userSession).saveOngoingRegistrationEntry(OngoingRegistrationEntry(pinConfirmation = input))
    verify(screen).openFacilitySelectionScreen()
  }

  @Test
  fun `while pin field is empty then the next button should remain disabled`() {
    uiEvents.onNext(RegistrationConfirmPinTextChanged(""))
    uiEvents.onNext(RegistrationConfirmPinTextChanged("1"))
    uiEvents.onNext(RegistrationConfirmPinTextChanged("12"))
    uiEvents.onNext(RegistrationConfirmPinTextChanged(""))

    verify(screen, times(2)).setNextButtonEnabled(false)
    verify(screen, times(1)).setNextButtonEnabled(true)
  }
}
