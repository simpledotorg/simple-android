package org.simple.clinic.registration.phone

import com.nhaarman.mockitokotlin2.any
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
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.user.finduser.FindUserResult.Found
import org.simple.clinic.user.finduser.FindUserResult.NetworkError
import org.simple.clinic.user.finduser.FindUserResult.NotFound
import org.simple.clinic.user.finduser.FindUserResult.UnexpectedError
import org.simple.clinic.user.finduser.UserLookup
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class RegistrationPhoneScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<RegistrationPhoneUi>()
  private val userSession = mock<UserSession>()
  private val numberValidator = LengthBasedNumberValidator(10,
      10,
      6,
      12)
  private val findUserWithPhoneNumber = mock<UserLookup>()
  private val facilitySync = mock<FacilitySync>()

  private val uiEvents: Subject<UiEvent> = PublishSubject.create<UiEvent>()
  private val userUuid = UUID.fromString("a5c55e97-dcad-4cd8-9832-4da9f7b3d4b7")
  private val defaultOngoingEntry = OngoingRegistrationEntry(uuid = userUuid)

  private lateinit var testFixture: MobiusTestFixture<RegistrationPhoneModel, RegistrationPhoneEvent, RegistrationPhoneEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when screen is created then existing details should be pre-filled`() {
    // given
    val ongoingEntry = defaultOngoingEntry.withPhoneNumber("123")

    // when
    setupController(ongoingRegistrationEntry = ongoingEntry)

    // then
    verify(ui).preFillUserDetails(ongoingEntry)
  }

  @Test
  fun `when proceed is clicked with a valid number then the ongoing entry should be updated and then the next screen should be opened`() {
    // given
    val validNumber = "1234567890"
    val entryWithPhoneNumber = defaultOngoingEntry.withPhoneNumber(validNumber)

    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)
    whenever(findUserWithPhoneNumber.find(validNumber)).doReturn(NotFound)

    // when
    setupController()
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(validNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(ui).openRegistrationNameEntryScreen(entryWithPhoneNumber)
  }

  @Test
  fun `proceed button clicks should only be accepted if the input phone number is valid`() {
    // given
    val invalidNumber = "12345"
    val validNumber = "1234567890"
    val entryWithValidNumber = defaultOngoingEntry.withPhoneNumber(validNumber)
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)
    whenever(findUserWithPhoneNumber.find(validNumber)) doReturn NotFound

    // when
    setupController()
    clearInvocations(userSession)
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(invalidNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verifyZeroInteractions(userSession)
    verify(ui, never()).openRegistrationNameEntryScreen(any())

    // when
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(validNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(ui).openRegistrationNameEntryScreen(entryWithValidNumber)
  }

  @Test
  fun `when proceed is clicked with an invalid number then an error should be shown`() {
    // given
    val invalidNumber = "12345"

    // when
    setupController()
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(invalidNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(ui).showInvalidNumberError()
    verify(ui, never()).openRegistrationNameEntryScreen(any())
  }

  @Test
  fun `when input text is changed then any visible errors should be removed`() {
    // when
    setupController()
    clearInvocations(ui)
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(""))

    // then
    verify(ui).hideAnyError()
  }

  @Test
  fun `when proceed is clicked with a valid phone number then a network call should be made to check if the phone number belongs to an existing user`() {
    // given
    val inputNumber = "1234567890"
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)
    whenever(findUserWithPhoneNumber.find(inputNumber)).doReturn(NetworkError)

    // when
    setupController()
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(ui).showProgressIndicator()
    verify(findUserWithPhoneNumber).find(inputNumber)
  }

  @Test
  fun `when the network call for checking phone number fails then an error should be shown`() {
    // given
    val inputNumber = "1234567890"
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)
    whenever(findUserWithPhoneNumber.find(inputNumber))
        .doReturn(UnexpectedError)
        .doReturn(NetworkError)

    // when
    setupController()
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    clearInvocations(ui)
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(ui).showProgressIndicator()
    verify(ui).hideProgressIndicator()
    verify(ui).showUnexpectedErrorMessage()

    clearInvocations(ui)

    // when
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(ui).showProgressIndicator()
    verify(ui).hideProgressIndicator()
    verify(ui).showNetworkErrorMessage()
  }

  @Test
  fun `when the phone number belongs to an existing user then an ongoing login entry should be created and login PIN entry screen should be opened`() {
    // given
    val inputNumber = "1234567890"
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
        updatedAt = null,
        capabilities = null
    )

    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)
    whenever(findUserWithPhoneNumber.find(inputNumber)).doReturn(Found(userUuid, userStatus))
    whenever(userSession.saveOngoingLoginEntry(entryToBeSaved)).doReturn(Completable.complete())

    // when
    setupController()
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    clearInvocations(ui)
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(userSession).saveOngoingLoginEntry(entryToBeSaved)
    verify(ui).openLoginPinEntryScreen()
    verify(ui, never()).showAccessDeniedScreen(inputNumber)
  }

  @Test
  fun `when the existing user is denied access then access denied screen should show`() {
    // given
    val inputNumber = "1234567890"
    val userStatus = UserStatus.DisapprovedForSyncing
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)
    whenever(findUserWithPhoneNumber.find(inputNumber)).doReturn(Found(userUuid, userStatus))

    // when
    setupController()
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(ui).showAccessDeniedScreen(inputNumber)
    verify(userSession, never()).saveOngoingLoginEntry(any())
    verify(ui, never()).openLoginPinEntryScreen()
  }

  @Test
  fun `when proceed is clicked then any existing error should be cleared`() {
    // given
    val inputNumber = "1234567890"
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)
    whenever(findUserWithPhoneNumber.find(inputNumber)).doReturn(NetworkError)

    // when
    setupController()
    clearInvocations(ui)
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(inputNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(ui, times(2)).hideAnyError()
  }

  @Test
  fun `when the screen is created and a local logged in user exists, show the logged out dialog if the user is unauthorized`() {
    // when
    setupController(isUserUnauthorized = true)

    // then
    verify(ui).showLoggedOutOfDeviceDialog()
  }

  @Test
  fun `when the screen is created and a local logged in user exists, do not show the logged out dialog if the user is unauthorized`() {
    // when
    setupController(isUserUnauthorized = false)

    // then
    verify(ui, never()).showLoggedOutOfDeviceDialog()
  }

  @Test
  fun `before a phone number is looked up, the facilities must be synced`() {
    // given
    val phoneNumber = "1234567890"
    whenever(findUserWithPhoneNumber.find(phoneNumber)) doReturn NetworkError
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.Success)

    // when
    setupController()
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(phoneNumber))
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(ui).showProgressIndicator()
    verify(facilitySync).pullWithResult()
  }

  @Test
  fun `when pulling the facilities fails, the number must not be looked up`() {
    // given
    val phoneNumber = "1234567890"
    whenever(findUserWithPhoneNumber.find(phoneNumber)) doReturn NetworkError
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.NetworkError)

    // when
    setupController()
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
    setupController()
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(phoneNumber))
    clearInvocations(ui)
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(ui).hideProgressIndicator()
    verify(ui).showNetworkErrorMessage()
    verify(findUserWithPhoneNumber, never()).find(phoneNumber)
  }

  @Test
  fun `when pulling the facilities fails with any other error, the unexpected error message must be shown`() {
    // given
    val phoneNumber = "1234567890"
    whenever(facilitySync.pullWithResult()) doReturn Single.just<FacilityPullResult>(FacilityPullResult.UnexpectedError)

    // when
    setupController()
    uiEvents.onNext(RegistrationPhoneNumberTextChanged(phoneNumber))
    clearInvocations(ui)
    uiEvents.onNext(RegistrationPhoneDoneClicked())

    // then
    verify(ui).hideProgressIndicator()
    verify(ui).showUnexpectedErrorMessage()
    verify(findUserWithPhoneNumber, never()).find(phoneNumber)
  }

  private fun setupController(
      ongoingRegistrationEntry: OngoingRegistrationEntry = defaultOngoingEntry,
      isUserUnauthorized: Boolean = false
  ) {
    whenever(userSession.isUserUnauthorized()) doReturn Observable.just(isUserUnauthorized)

    val uuidGenerator = FakeUuidGenerator.fixed(userUuid)

    val uiRenderer = RegistrationPhoneUiRenderer(ui)

    val effectHandler = RegistrationPhoneEffectHandler(
        uiActions = ui,
        schedulers = TrampolineSchedulersProvider(),
        userSession = userSession,
        uuidGenerator = uuidGenerator,
        numberValidator = numberValidator,
        facilitySync = facilitySync,
        userLookup = findUserWithPhoneNumber
    )

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = RegistrationPhoneModel.create(ongoingRegistrationEntry),
        init = RegistrationPhoneInit(),
        update = RegistrationPhoneUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )
    testFixture.start()
  }
}
