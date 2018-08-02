package org.simple.clinic.registration.confirmpin

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.check
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

  private val uiEvents = PublishSubject.create<UiEvent>()!!
  private val screen = mock<RegistrationConfirmPinScreen>()
  private val userSession = mock<UserSession>()
  private val registrationScheduler = mock<RegistrationScheduler>()

  private lateinit var controller: RegistrationConfirmPinScreenController

  @Before
  fun setUp() {
    controller = RegistrationConfirmPinScreenController(userSession, registrationScheduler)

    whenever(userSession.loginFromOngoingRegistrationEntry()).thenReturn(Completable.complete())
    whenever(registrationScheduler.schedule()).thenReturn(Completable.complete())

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when next button is clicked then ongoing entry should be updated with the input pin and the next screen should be opened`() {
    val input = "1234"

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationConfirmPinTextChanged(input))
    uiEvents.onNext(RegistrationConfirmPinNextClicked())

    verify(userSession).saveOngoingRegistrationEntry(check {
      assertThat(it.pinConfirmation).isEqualTo(input)
      assertThat(it.createdAt).isNotNull()
    })
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

  @Test
  fun `when screen is created then user's existing details should be pre-filled`() {
    val ongoingEntry = OngoingRegistrationEntry(
        fullName = "Ashok Kumar",
        phoneNumber = "1234567890",
        pin = "1234",
        pinConfirmation = "1234")
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(ongoingEntry))

    uiEvents.onNext(RegistrationConfirmPinScreenCreated())

    verify(screen).preFillUserDetails(ongoingEntry)
  }
}
