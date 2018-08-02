package org.simple.clinic.registration.phone

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
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
  fun `when screen is created and an existing ongoing entry is absent then an empty ongoing entry should be created`() {
    whenever(userSession.saveOngoingRegistrationEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.isOngoingRegistrationEntryPresent()).thenReturn(Single.just(false))
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))

    uiEvents.onNext(RegistrationPhoneScreenCreated())

    verify(userSession).saveOngoingRegistrationEntry(argThat { uuid != null })
  }

  @Test
  fun `when screen is created and an existing ongoing entry is present then an empty ongoing entry should not be created`() {
    whenever(userSession.saveOngoingRegistrationEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.isOngoingRegistrationEntryPresent()).thenReturn(Single.just(true))

    uiEvents.onNext(RegistrationPhoneScreenCreated())

    verify(userSession, never()).saveOngoingRegistrationEntry(any())
  }

  @Test
  fun `when screen is created then existing details should be pre-filled`() {
    val ongoingEntry = OngoingRegistrationEntry(phoneNumber = "123")
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(userSession.isOngoingRegistrationEntryPresent()).thenReturn(Single.just(true))

    uiEvents.onNext(RegistrationPhoneScreenCreated())

    verify(userSession, never()).saveOngoingRegistrationEntry(any())
    verify(screen).preFillUserDetails(argThat { phoneNumber == ongoingEntry.phoneNumber })
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
