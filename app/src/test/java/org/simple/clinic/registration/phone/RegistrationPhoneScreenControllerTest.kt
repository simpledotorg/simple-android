package org.simple.clinic.registration.phone

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_SHORT
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.VALID
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.MOBILE
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.finduser.FindUserResult
import org.simple.clinic.user.finduser.FindUserResult.Found
import org.simple.clinic.user.finduser.FindUserResult.NetworkError
import org.simple.clinic.user.finduser.FindUserResult.NotFound
import org.simple.clinic.user.finduser.FindUserResult.UnexpectedError
import org.simple.clinic.user.finduser.FindUserWithPhoneNumber
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent

@RunWith(JUnitParamsRunner::class)
class RegistrationPhoneScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<RegistrationPhoneScreen>()
  private val userSession = mock<UserSession>()
  private val numberValidator = mock<PhoneNumberValidator>()
  private val findUserWithPhoneNumber = mock<FindUserWithPhoneNumber>()

  private val uiEvents: Subject<UiEvent> = PublishSubject.create<UiEvent>()

  private val controller: RegistrationPhoneScreenController = RegistrationPhoneScreenController(
      userSession = userSession,
      findUserWithPhoneNumber = findUserWithPhoneNumber,
      numberValidator = numberValidator
  )

  @Before
  fun setUp() {
    whenever(userSession.isOngoingRegistrationEntryPresent())
        .doReturn(Single.never())
    whenever(userSession.isUserUnauthorized())
        .doReturn(Observable.never())

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen is created and an existing ongoing entry is absent then an empty ongoing entry should be created`() {
    whenever(userSession.saveOngoingRegistrationEntry(any())).doReturn(Completable.complete())
    whenever(userSession.isOngoingRegistrationEntryPresent()).doReturn(Single.just(false))
    whenever(userSession.ongoingRegistrationEntry()).doReturn(Single.just(OngoingRegistrationEntry()))

    uiEvents.onNext(RegistrationPhoneScreenCreated())

    verify(userSession).saveOngoingRegistrationEntry(argThat { uuid != null })
  }

  @Test
  fun `when screen is created and an existing ongoing entry is present then an empty ongoing entry should not be created`() {
    whenever(userSession.saveOngoingRegistrationEntry(any())).doReturn(Completable.complete())
    whenever(userSession.isOngoingRegistrationEntryPresent()).doReturn(Single.just(true))
    whenever(userSession.ongoingRegistrationEntry()).doReturn(Single.never())

    uiEvents.onNext(RegistrationPhoneScreenCreated())

    verify(userSession, never()).saveOngoingRegistrationEntry(any())
  }

  @Test
  fun `when screen is created then existing details should be pre-filled`() {
    val ongoingEntry = OngoingRegistrationEntry(phoneNumber = "123")
    whenever(userSession.ongoingRegistrationEntry()).doReturn(Single.just(ongoingEntry))
    whenever(userSession.isOngoingRegistrationEntryPresent()).doReturn(Single.just(true))

    uiEvents.onNext(RegistrationPhoneScreenCreated())

    verify(userSession, never()).saveOngoingRegistrationEntry(any())
    verify(screen).preFillUserDetails(argThat { phoneNumber == ongoingEntry.phoneNumber })
  }

  @Test
  fun `when proceed is clicked with a valid number then the ongoing entry should be updated and then the next screen should be opened`() {
    val validNumber = "1234567890"
    whenever(userSession.ongoingRegistrationEntry()).doReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(phoneNumber = validNumber))).doReturn(Completable.complete())
    whenever(numberValidator.validate(validNumber, MOBILE)).doReturn(VALID)
    whenever(findUserWithPhoneNumber.find(validNumber)).doReturn(Single.just<FindUserResult>(NotFound))

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(validNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(userSession).saveOngoingRegistrationEntry(OngoingRegistrationEntry(phoneNumber = validNumber))
    verify(screen).openRegistrationNameEntryScreen()
  }

  @Test
  fun `proceed button clicks should only be accepted if the input phone number is valid`() {
    val invalidNumber = "12345"
    val validNumber = "1234567890"
    whenever(userSession.ongoingRegistrationEntry()).doReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(phoneNumber = validNumber))).doReturn(Completable.complete())
    whenever(findUserWithPhoneNumber.find(validNumber)).doReturn(Single.just<FindUserResult>(NotFound))

    whenever(numberValidator.validate(invalidNumber, MOBILE)).doReturn(LENGTH_TOO_SHORT)
    whenever(numberValidator.validate(validNumber, MOBILE)).doReturn(VALID)

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
    whenever(numberValidator.validate(invalidNumber, MOBILE)).doReturn(LENGTH_TOO_SHORT)

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
    whenever(findUserWithPhoneNumber.find(inputNumber)).doReturn(Single.never())
    whenever(numberValidator.validate(inputNumber, MOBILE)).doReturn(VALID)

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(screen).showProgressIndicator()
    verify(findUserWithPhoneNumber).find(inputNumber)
  }

  @Test
  fun `when the network call for checking phone number fails then an error should be shown`() {
    val inputNumber = "1234567890"

    whenever(findUserWithPhoneNumber.find(inputNumber))
        .doReturn(Single.just<FindUserResult>(UnexpectedError))
        .doReturn(Single.just<FindUserResult>(NetworkError))

    whenever(numberValidator.validate(inputNumber, MOBILE)).doReturn(VALID)

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
    val userPayload = PatientMocker.loggedInUserPayload(phone = inputNumber)

    whenever(findUserWithPhoneNumber.find(inputNumber)).doReturn(Single.just<FindUserResult>(Found(userPayload)))
    whenever(numberValidator.validate(inputNumber, MOBILE)).doReturn(VALID)
    whenever(userSession.saveOngoingLoginEntry(any())).doReturn(Completable.complete())
    whenever(userSession.clearOngoingRegistrationEntry()).doReturn(Completable.complete())

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(userSession).saveOngoingLoginEntry(OngoingLoginEntry(
        uuid = userPayload.uuid,
        phoneNumber = inputNumber,
        pin = null,
        fullName = userPayload.fullName,
        pinDigest = userPayload.pinDigest,
        registrationFacilityUuid = userPayload.registrationFacilityId,
        status = userPayload.status,
        createdAt = userPayload.createdAt,
        updatedAt = userPayload.updatedAt
    ))
    verify(userSession).clearOngoingRegistrationEntry()
    verify(screen).openLoginPinEntryScreen()
  }

  // TODO 26-07-19 : Check validity of this test since it no longer makes a network call (facility sync)
  @Test
  fun `when the phone number belongs to an existing user and creating ongoing entry fails, an error should be shown`() {
    val inputNumber = "1234567890"
    val userPayload = PatientMocker.loggedInUserPayload(phone = inputNumber)

    whenever(findUserWithPhoneNumber.find(inputNumber)).doReturn(Single.just<FindUserResult>(Found(userPayload)))
    whenever(numberValidator.validate(inputNumber, MOBILE)).doReturn(VALID)
    whenever(userSession.clearOngoingRegistrationEntry()).doReturn(Completable.complete())
    whenever(userSession.saveOngoingLoginEntry(any())).doReturn(Completable.error(RuntimeException()))

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(userSession).saveOngoingLoginEntry(OngoingLoginEntry(
        uuid = userPayload.uuid,
        phoneNumber = inputNumber,
        pin = null,
        fullName = userPayload.fullName,
        pinDigest = userPayload.pinDigest,
        registrationFacilityUuid = userPayload.registrationFacilityId,
        status = userPayload.status,
        createdAt = userPayload.createdAt,
        updatedAt = userPayload.updatedAt
    ))
    verify(screen, never()).openLoginPinEntryScreen()
    verify(screen).showUnexpectedErrorMessage()
  }

  @Test
  fun `when proceed is clicked then any existing error should be cleared`() {
    val inputNumber = "1234567890"
    whenever(numberValidator.validate(inputNumber, MOBILE)).doReturn(VALID)
    whenever(findUserWithPhoneNumber.find(inputNumber)).doReturn(Single.never())

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(screen, times(2)).hideAnyError()
  }

  @Test
  @Parameters(value = [
    "true|true",
    "false|false"
  ])
  fun `when the screen is created and a local logged in user exists, show the logged out dialog if the user is unauthorized`(
      isUserUnauthorized: Boolean,
      shouldShowLoggedOutDialog: Boolean
  ) {
    whenever(userSession.isUserUnauthorized()).doReturn(Observable.just(isUserUnauthorized))

    uiEvents.onNext(RegistrationPhoneScreenCreated())

    if (shouldShowLoggedOutDialog) {
      verify(screen).showLoggedOutOfDeviceDialog()
    } else {
      verify(screen, never()).showLoggedOutOfDeviceDialog()
    }
  }
}
