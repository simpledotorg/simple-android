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
import org.simple.clinic.registration.FindUserResult
import org.simple.clinic.registration.SaveUserLocallyResult
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_SHORT
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.VALID
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.MOBILE
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class RegistrationPhoneScreenControllerTest {

  private val screen = mock<RegistrationPhoneScreen>()
  private val userSession = mock<UserSession>()
  private val numberValidator = mock<PhoneNumberValidator>()

  private val uiEvents = PublishSubject.create<UiEvent>()!!

  private lateinit var controller: RegistrationPhoneScreenController

  @Before
  fun setUp() {
    controller = RegistrationPhoneScreenController(userSession, numberValidator)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen is created and existing ongoing entry is present, then the local user must be cleared`() {
    whenever(userSession.clearLoggedInUser()).thenReturn(Completable.complete())
    whenever(userSession.isOngoingRegistrationEntryPresent()).thenReturn(Single.just(true))
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))

    uiEvents.onNext(RegistrationPhoneScreenCreated())

    verify(userSession).clearLoggedInUser()
  }

  @Test
  fun `when screen is created and an existing ongoing entry is absent then an empty ongoing entry should be created`() {
    whenever(userSession.saveOngoingRegistrationEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.isOngoingRegistrationEntryPresent()).thenReturn(Single.just(false))
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.clearLoggedInUser()).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationPhoneScreenCreated())

    verify(userSession).saveOngoingRegistrationEntry(argThat { uuid != null })
  }

  @Test
  fun `when screen is created and an existing ongoing entry is present then an empty ongoing entry should not be created`() {
    whenever(userSession.saveOngoingRegistrationEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.isOngoingRegistrationEntryPresent()).thenReturn(Single.just(true))
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.never())

    uiEvents.onNext(RegistrationPhoneScreenCreated())

    verify(userSession, never()).saveOngoingRegistrationEntry(any())
  }

  @Test
  fun `when screen is created then existing details should be pre-filled`() {
    val ongoingEntry = OngoingRegistrationEntry(phoneNumber = "123")
    whenever(userSession.clearLoggedInUser()).thenReturn(Completable.complete())
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
    whenever(numberValidator.validate(validNumber, MOBILE)).thenReturn(VALID)
    whenever(userSession.findExistingUser(validNumber)).thenReturn(Single.just(FindUserResult.NotFound()))

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
    whenever(userSession.findExistingUser(validNumber)).thenReturn(Single.just(FindUserResult.NotFound()))

    whenever(numberValidator.validate(invalidNumber, MOBILE)).thenReturn(LENGTH_TOO_SHORT)
    whenever(numberValidator.validate(validNumber, MOBILE)).thenReturn(VALID)

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(invalidNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(validNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(numberValidator, times(4)).validate(any(), any())
    verify(userSession).saveOngoingRegistrationEntry(OngoingRegistrationEntry(phoneNumber = validNumber))
    verify(screen, times(1)).openRegistrationNameEntryScreen()
  }

  @Test
  fun `when proceed is clicked with an invalid number then an error should be shown`() {
    val invalidNumber = "12345"
    whenever(numberValidator.validate(invalidNumber, MOBILE)).thenReturn(LENGTH_TOO_SHORT)

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(invalidNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(screen).showInvalidNumberError()
    verify(userSession, never()).saveOngoingRegistrationEntry(any())
    verify(screen, never()).openRegistrationNameEntryScreen()
  }

  @Test
  fun `when input text is changed then any visible errors should be removed`() {
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(""))
    verify(screen).hideAnyError()
  }

  @Test
  fun `when proceed is clicked with a valid phone number then a network call should be made to check if the phone number belongs to an existing user`() {
    val inputNumber = "1234567890"
    whenever(userSession.findExistingUser(inputNumber)).thenReturn(Single.never())
    whenever(numberValidator.validate(inputNumber, MOBILE)).thenReturn(VALID)

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(screen).showProgressIndicator()
    verify(userSession).findExistingUser(inputNumber)
  }

  @Test
  fun `when the network call for checking phone number fails then an error should be shown`() {
    val inputNumber = "1234567890"

    whenever(userSession.findExistingUser(inputNumber))
        .thenReturn(Single.just(FindUserResult.UnexpectedError()))
        .thenReturn(Single.just(FindUserResult.NetworkError()))

    whenever(numberValidator.validate(inputNumber, MOBILE)).thenReturn(VALID)

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(screen, times(2)).showProgressIndicator()
    verify(screen, times(2)).hideProgressIndicator()
    verify(screen).showUnexpectedErrorMessage()
    verify(screen).showNetworkErrorMessage()
  }

  @Test
  fun `when the phone number belongs to an existing user then an ongoing login entry should be created and login PIN entry screen should be opened`() {
    val inputNumber = "1234567890"
    val mockUser = mock<LoggedInUserPayload>()
    val userId = UUID.randomUUID()
    whenever(mockUser.uuid).thenReturn(userId)
    whenever(mockUser.phoneNumber).thenReturn(inputNumber)

    whenever(userSession.findExistingUser(inputNumber)).thenReturn(Single.just(FindUserResult.Found(mockUser)))
    whenever(userSession.syncFacilityAndSaveUser(any())).thenReturn(Single.just(SaveUserLocallyResult.Success()))
    whenever(numberValidator.validate(inputNumber, MOBILE)).thenReturn(VALID)
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.clearOngoingRegistrationEntry()).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(userSession).saveOngoingLoginEntry(OngoingLoginEntry(uuid = userId, phoneNumber = inputNumber))
    verify(userSession).syncFacilityAndSaveUser(mockUser)
    verify(userSession).clearOngoingRegistrationEntry()
    verify(screen).openLoginPinEntryScreen()
  }

  @Test
  fun `when the phone number belongs to an existing user and save user locally fails, an error should be shown`() {
    val inputNumber = "1234567890"
    val mockUser = mock<LoggedInUserPayload>()
    val userId = UUID.randomUUID()
    whenever(mockUser.uuid).thenReturn(userId)
    whenever(mockUser.phoneNumber).thenReturn(inputNumber)

    whenever(userSession.findExistingUser(inputNumber)).thenReturn(Single.just(FindUserResult.Found(mockUser)))

    whenever(userSession.syncFacilityAndSaveUser(any())).thenReturn(
        Single.just(SaveUserLocallyResult.NetworkError()),
        Single.just(SaveUserLocallyResult.UnexpectedError())
    )

    whenever(numberValidator.validate(inputNumber, MOBILE)).thenReturn(VALID)
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.clearOngoingRegistrationEntry()).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(userSession, times(2)).saveOngoingLoginEntry(OngoingLoginEntry(uuid = userId, phoneNumber = inputNumber))
    verify(userSession, times(2)).syncFacilityAndSaveUser(mockUser)
    verify(userSession, times(2)).clearOngoingRegistrationEntry()
    verify(screen, never()).openLoginPinEntryScreen()
    verify(screen).showNetworkErrorMessage()
    verify(screen).showUnexpectedErrorMessage()
  }

  @Test
  fun `when proceed is clicked then any existing error should be cleared`() {
    val inputNumber = "1234567890"
    whenever(numberValidator.validate(inputNumber, MOBILE)).thenReturn(VALID)
    whenever(userSession.findExistingUser(inputNumber)).thenReturn(Single.never())

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(screen, times(2)).hideAnyError()
  }
}
