package org.simple.clinic.registration.phone

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.mock
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

class RegistrationPhoneScreenControllerTest {

  val uiEvents = PublishSubject.create<UiEvent>()!!
  val screen = mock<RegistrationPhoneScreen>()
  val userSession = mock<UserSession>()

  private lateinit var controller: RegistrationPhoneScreenController

  @Before
  fun setUp() {
    controller = RegistrationPhoneScreenController(userSession)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen is created then an empty ongoing entry should be created`() {
    whenever(userSession.saveOngoingRegistrationEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationPhoneScreenCreated())

    verify(userSession).saveOngoingRegistrationEntry(argThat { uuid != null })
  }

  @Test
  fun `when proceed button is clicked then the ongoing entry should be updated with the input phone number and the next screen should be opened`() {
    val input = "999999"

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(phoneNumber = input))).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(input))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(userSession).saveOngoingRegistrationEntry(OngoingRegistrationEntry(phoneNumber = input))
    verify(screen).openRegistrationNameEntryScreen()
  }
}
