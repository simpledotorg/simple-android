package org.simple.clinic.registration.phone

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.user.finduser.FindUserResult
import org.simple.clinic.user.finduser.FindUserResult.Found
import org.simple.clinic.user.finduser.FindUserResult.NetworkError
import org.simple.clinic.user.finduser.FindUserResult.NotFound
import org.simple.clinic.user.finduser.FindUserResult.UnexpectedError
import org.simple.clinic.user.finduser.UserLookup
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class RegistrationPhoneScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<RegistrationPhoneScreen>()
  private val userSession = mock<UserSession>()
  private val numberValidator = IndianPhoneNumberValidator()
  private val findUserWithPhoneNumber = mock<UserLookup>()
  private val facilitySync = mock<FacilitySync>()

  private val uiEvents: Subject<UiEvent> = PublishSubject.create<UiEvent>()

  private val controller: RegistrationPhoneScreenController = RegistrationPhoneScreenController(
      userSession = userSession,
      userLookup = findUserWithPhoneNumber,
      numberValidator = numberValidator,
      facilitySync = facilitySync
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
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)
    whenever(userSession.ongoingRegistrationEntry()).doReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(phoneNumber = validNumber))).doReturn(Completable.complete())
    whenever(findUserWithPhoneNumber.find(validNumber)).doReturn(NotFound)

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(validNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(userSession).saveOngoingRegistrationEntry(OngoingRegistrationEntry(phoneNumber = validNumber))
    verify(screen).openRegistrationNameEntryScreen()
  }

  @Test
  fun `proceed button clicks should only be accepted if the input phone number is valid`() {
    val invalidNumber = "12345"
    val validNumber = "1234567890"
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)
    whenever(userSession.ongoingRegistrationEntry()).doReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(phoneNumber = validNumber))).doReturn(Completable.complete())
    whenever(findUserWithPhoneNumber.find(validNumber)) doReturn NotFound

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(invalidNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())
    verifyZeroInteractions(userSession)

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(validNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())
    verify(userSession).saveOngoingRegistrationEntry(OngoingRegistrationEntry(phoneNumber = validNumber))
    verify(screen).openRegistrationNameEntryScreen()
  }

  @Test
  fun `when proceed is clicked with an invalid number then an error should be shown`() {
    val invalidNumber = "12345"
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
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)
    whenever(findUserWithPhoneNumber.find(inputNumber)).doReturn(NetworkError)

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(screen).showProgressIndicator()
    verify(findUserWithPhoneNumber).find(inputNumber)
  }

  @Test
  fun `when the network call for checking phone number fails then an error should be shown`() {
    val inputNumber = "1234567890"

    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)
    whenever(findUserWithPhoneNumber.find(inputNumber))
        .doReturn(UnexpectedError)
        .doReturn(NetworkError)

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))

    uiEvents.onNext(RegistrationPhoneDoneClicked())
    verify(screen).showProgressIndicator()
    verify(screen).hideProgressIndicator()
    verify(screen).showUnexpectedErrorMessage()

    clearInvocations(screen)

    uiEvents.onNext(RegistrationPhoneDoneClicked())
    verify(screen).showProgressIndicator()
    verify(screen).hideProgressIndicator()
    verify(screen).showNetworkErrorMessage()
  }

  @Test
  fun `when the phone number belongs to an existing user then an ongoing login entry should be created and login PIN entry screen should be opened`() {
    val inputNumber = "1234567890"
    val userUuid = UUID.fromString("61804b34-d558-4191-b2ac-0c77b854eb81")
    val userStatus = UserStatus.ApprovedForSyncing
    val entryToBeSaved = OngoingLoginEntry(
        uuid = userUuid,
        phoneNumber = inputNumber,
        pin = null,
        fullName = null,
        pinDigest = null,
        registrationFacilityUuid = null,
        status = userStatus,
        createdAt = null,
        updatedAt = null
    )

    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)
    whenever(findUserWithPhoneNumber.find(inputNumber)).doReturn(Found(userUuid, userStatus))
    whenever(userSession.saveOngoingLoginEntry(entryToBeSaved)).doReturn(Completable.complete())
    whenever(userSession.clearOngoingRegistrationEntry()).doReturn(Completable.complete())

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(userSession).saveOngoingLoginEntry(entryToBeSaved)
    verify(userSession).clearOngoingRegistrationEntry()
    verify(screen).openLoginPinEntryScreen()
    verify(screen, never()).showAccessDeniedScreen(inputNumber)
  }

  @Test
  fun `when the phone number belongs to an existing user and creating ongoing entry fails, an error should be shown`() {
    val inputNumber = "1234567890"
    val userUuid = UUID.fromString("61804b34-d558-4191-b2ac-0c77b854eb81")
    val status = UserStatus.ApprovedForSyncing

    val entryToBeSaved = OngoingLoginEntry(
        uuid = userUuid,
        phoneNumber = inputNumber,
        pin = null,
        fullName = null,
        pinDigest = null,
        registrationFacilityUuid = null,
        status = status,
        createdAt = null,
        updatedAt = null
    )

    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)
    whenever(findUserWithPhoneNumber.find(inputNumber)).doReturn(Found(userUuid, status))
    whenever(userSession.clearOngoingRegistrationEntry()).doReturn(Completable.complete())
    whenever(userSession.saveOngoingLoginEntry(entryToBeSaved)).doReturn(Completable.error(RuntimeException()))

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(userSession).saveOngoingLoginEntry(entryToBeSaved)
    verify(screen, never()).openLoginPinEntryScreen()
    verify(screen, never()).showAccessDeniedScreen(inputNumber)
    verify(screen).showUnexpectedErrorMessage()
  }

  @Test
  fun `when the existing user is denied access then access denied screen should show`() {
    val inputNumber = "1234567890"
    val userUuid = UUID.fromString("597fbc43-88c7-4017-b4c1-e600980945b0")
    val userStatus = UserStatus.DisapprovedForSyncing
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)
    whenever(findUserWithPhoneNumber.find(inputNumber)).doReturn(Found(userUuid, userStatus))

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(screen).showAccessDeniedScreen(inputNumber)
    verify(userSession, never()).saveOngoingLoginEntry(any())
    verify(userSession, never()).clearOngoingRegistrationEntry()
    verify(screen, never()).openLoginPinEntryScreen()
  }

  @Test
  fun `when proceed is clicked then any existing error should be cleared`() {
    val inputNumber = "1234567890"
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)
    whenever(findUserWithPhoneNumber.find(inputNumber)).doReturn(NetworkError)

    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    verify(screen, times(2)).hideAnyError()
  }

  @Test
  fun `when the screen is created and a local logged in user exists, show the logged out dialog if the user is unauthorized`() {
    whenever(userSession.isUserUnauthorized()).doReturn(Observable.just(true))

    uiEvents.onNext(RegistrationPhoneScreenCreated())

    verify(screen).showLoggedOutOfDeviceDialog()
  }

  @Test
  fun `when the screen is created and a local logged in user exists, do not show the logged out dialog if the user is unauthorized`() {
    whenever(userSession.isUserUnauthorized()).doReturn(Observable.just(false))

    uiEvents.onNext(RegistrationPhoneScreenCreated())

    verify(screen, never()).showLoggedOutOfDeviceDialog()
  }

  @Test
  fun `before a phone number is looked up, the facilities must be synced`() {
    // given
    val phoneNumber = "1234567890"
    whenever(findUserWithPhoneNumber.find(phoneNumber)) doReturn NetworkError
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)

    // when
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(phoneNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(screen).showProgressIndicator()
    verify(facilitySync).pullWithResult()
  }

  @Test
  fun `when pulling the facilities fails, the number must not be looked up`() {
    // given
    val phoneNumber = "1234567890"
    whenever(findUserWithPhoneNumber.find(phoneNumber)) doReturn NetworkError
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.NetworkError)

    // when
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(phoneNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(findUserWithPhoneNumber, never()).find(phoneNumber)
  }

  @Test
  fun `when pulling the facilities fails with a network error, the network error message must be shown`() {
    // given
    val phoneNumber = "1234567890"
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.NetworkError)

    // when
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(phoneNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(screen).hideProgressIndicator()
    verify(screen).showNetworkErrorMessage()
    verify(findUserWithPhoneNumber, never()).find(phoneNumber)
  }

  @Test
  fun `when pulling the facilities fails with any other error, the unexpected error message must be shown`() {
    // given
    val phoneNumber = "1234567890"
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.UnexpectedError)

    // when
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(phoneNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(screen).hideProgressIndicator()
    verify(screen).showUnexpectedErrorMessage()
    verify(findUserWithPhoneNumber, never()).find(phoneNumber)
  }
}
