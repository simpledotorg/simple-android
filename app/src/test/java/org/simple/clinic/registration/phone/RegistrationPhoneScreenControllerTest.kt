package org.simple.clinic.registration.phone

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.inOrder
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

class RegistrationPhoneScreenControllerTest {

  private val uiEvents = PublishSubject.create<UiEvent>()!!
  private val screen = mock<RegistrationPhoneScreen>()
  private val userSession = mock<UserSession>()
  private val numberValidator = mock<PhoneNumberValidator>()

  private lateinit var controller: RegistrationPhoneScreenController

  @Before
  fun setUp() {
    controller = RegistrationPhoneScreenController(userSession, numberValidator)

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
  fun `when proceed is clicked with a valid number then the ongoing entry should be updated and then the next screen should be opened`() {
    val validNumber = "1234567890"
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(phoneNumber = validNumber))).thenReturn(Completable.complete())
    whenever(numberValidator.isValid(validNumber)).thenReturn(true)

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(validNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    val inOrder = inOrder(userSession, screen)
    inOrder.verify(userSession).saveOngoingRegistrationEntry(OngoingRegistrationEntry(phoneNumber = validNumber))
    inOrder.verify(screen).openRegistrationNameEntryScreen()
  }

  @Test
  fun `proceed button clicks should only be accepted if the input phone number is valid`() {
    val invalidNumber = "12345"
    val validNumber = "1234567890"
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(phoneNumber = validNumber))).thenReturn(Completable.complete())

    whenever(numberValidator.isValid(invalidNumber)).thenReturn(false)
    whenever(numberValidator.isValid(validNumber)).thenReturn(true)

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(invalidNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(validNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(numberValidator, times(4)).isValid(any())
    verify(userSession).saveOngoingRegistrationEntry(OngoingRegistrationEntry(phoneNumber = validNumber))
    verify(screen, times(1)).openRegistrationNameEntryScreen()
  }

  @Test
  fun `when proceed is clicked with an invalid number then an error should be shown`() {
    val invalidNumber = "12345"
    whenever(numberValidator.isValid(invalidNumber)).thenReturn(false)

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(invalidNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(screen).showInvalidNumberError()
    verify(userSession, never()).saveOngoingRegistrationEntry(any())
    verify(screen, never()).openRegistrationNameEntryScreen()
  }

  @Test
  fun `when input text is changed then any visible errors should be reset`() {
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(""))
    verify(screen).hideInvalidNumberError()
  }
}
