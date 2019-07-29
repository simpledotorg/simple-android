package org.simple.clinic.login.pin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.clearInvocations
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.RequestLoginOtp
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import java.util.UUID

class LoginPinScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<LoginPinScreen>()
  private val userSession = mock<UserSession>()
  private val requestLoginOtp = mock<RequestLoginOtp>()
  private val facilitySync = mock<FacilitySync>()

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val loginUserUuid = UUID.fromString("582099ce-e146-4aa5-8302-85dbbfa1b407")
  private val ongoingLoginEntry = OngoingLoginEntry(
      uuid = loginUserUuid,
      pinDigest = "",
      phoneNumber = "",
      fullName = "",
      registrationFacilityUuid = UUID.randomUUID(),
      status = UserStatus.WaitingForApproval,
      createdAt = Instant.now(),
      updatedAt = Instant.now()
  )

  private val controller = LoginPinScreenController(
      userSession = userSession,
      requestLoginOtp = requestLoginOtp,
      facilitySync = facilitySync
  )

  @Before
  fun setUp() {
    whenever(requestLoginOtp.requestForUser(loginUserUuid))
        .thenReturn(Single.never())
    whenever(facilitySync.pullWithResult())
        .thenReturn(Single.never())
    whenever(userSession.storeUser(any(), any()))
        .thenReturn(Completable.never())
    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts, show phone number`() {
    val phoneNumber = "phone-number"
    whenever(userSession.ongoingLoginEntry())
        .thenReturn(Single.just(ongoingLoginEntry.copy(phoneNumber = phoneNumber)))

    uiEvents.onNext(PinScreenCreated())

    verify(screen).showPhoneNumber(phoneNumber)
  }

  @Test
  fun `if request otp api call throws any errors, show errors`() {
    whenever(userSession.ongoingLoginEntry())
        .thenReturn(Single.just(ongoingLoginEntry))
    whenever(userSession.saveOngoingLoginEntry(any()))
        .thenReturn(Completable.complete())
    whenever(facilitySync.pullWithResult())
        .thenReturn(Single.just(FacilityPullResult.Success()))
    whenever(requestLoginOtp.requestForUser(loginUserUuid))
        .thenReturn(
            Single.just(RequestLoginOtp.Result.NetworkError),
            Single.just(RequestLoginOtp.Result.OtherError(RuntimeException()))
        )

    val enteredPin = "0000"

    uiEvents.onNext(LoginPinAuthenticated(enteredPin))
    verify(requestLoginOtp).requestForUser(loginUserUuid)
    verify(screen).showNetworkError()
    verify(screen, never()).openHomeScreen()

    clearInvocations(screen, requestLoginOtp)

    uiEvents.onNext(LoginPinAuthenticated(enteredPin))
    verify(requestLoginOtp).requestForUser(loginUserUuid)
    verify(screen).showUnexpectedError()
    verify(screen, never()).openHomeScreen()
  }

  @Test
  fun `when back is clicked, the local ongoing login entry must be cleared`() {
    whenever(userSession.clearOngoingLoginEntry()).thenReturn(Completable.complete())
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PinBackClicked())
    verify(userSession).clearOngoingLoginEntry()
  }

  @Test
  fun `when the screen is created, the pin digest to verify must be forwarded to the screen`() {
    val pinDigest = "digest"
    whenever(userSession.ongoingLoginEntry())
        .thenReturn(Single.just(ongoingLoginEntry.copy(pinDigest = pinDigest)))

    uiEvents.onNext(PinScreenCreated())

    verify(screen).submitWithPinDigest(pinDigest)
  }

  @Test
  fun `when PIN is submitted, update the saved login entry with the new PIN`() {
    // given
    val newPin = "new-pin"
    whenever(userSession.ongoingLoginEntry())
        .thenReturn(Single.just(ongoingLoginEntry))
    whenever(userSession.saveOngoingLoginEntry(ongoingLoginEntry.copy(pin = newPin)))
        .thenReturn(Completable.complete())

    // when
    uiEvents.onNext(LoginPinAuthenticated(newPin))

    // then
    verify(userSession).saveOngoingLoginEntry(ongoingLoginEntry.copy(pin = newPin))
  }

  @Test
  fun `when PIN is submitted and sync facilities fails, show errors`() {
    // given
    whenever(userSession.ongoingLoginEntry())
        .thenReturn(Single.just(ongoingLoginEntry))
    whenever(userSession.saveOngoingLoginEntry(any()))
        .thenReturn(Completable.complete())
    whenever(requestLoginOtp.requestForUser(loginUserUuid))
        .thenReturn(Single.just(RequestLoginOtp.Result.NetworkError))
    whenever(facilitySync.pullWithResult())
        .thenReturn(
            Single.just(FacilityPullResult.NetworkError()),
            Single.just(FacilityPullResult.UnexpectedError())
        )

    // when
    uiEvents.onNext(LoginPinAuthenticated("0000"))
    verify(screen).showNetworkError()

    clearInvocations(screen)

    uiEvents.onNext(LoginPinAuthenticated("0000"))
    verify(screen).showUnexpectedError()

    verify(requestLoginOtp, never()).requestForUser(loginUserUuid)
  }

  @Test
  fun `when PIN is submitted, sync facilities, request login OTP, save the logged in user and open the home screen`() {
    // given
    val registrationFacilityUuid = UUID.fromString("5314616f-35bb-4a4d-99b1-13f5849de82e")
    val phoneNumber = "phone number"
    val pinDigest = "pin digest"
    val fullName = "name"
    val status = UserStatus.ApprovedForSyncing
    val createdAt = Instant.parse("2019-07-25T06:01:03.325Z")
    val updatedAt = Instant.parse("2019-07-26T06:01:03.325Z")
    val ongoingLoginEntry = ongoingLoginEntry
        .copy(
            phoneNumber = phoneNumber,
            pinDigest = pinDigest,
            fullName = fullName,
            registrationFacilityUuid = registrationFacilityUuid,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    val expectedUser = User(
        uuid = loginUserUuid,
        fullName = fullName,
        phoneNumber = phoneNumber,
        pinDigest = pinDigest,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        loggedInStatus = User.LoggedInStatus.OTP_REQUESTED
    )

    whenever(userSession.ongoingLoginEntry())
        .thenReturn(Single.just(ongoingLoginEntry))
    whenever(userSession.saveOngoingLoginEntry(any()))
        .thenReturn(Completable.complete())
    whenever(requestLoginOtp.requestForUser(loginUserUuid))
        .thenReturn(Single.just(RequestLoginOtp.Result.Success))
    whenever(facilitySync.pullWithResult())
        .thenReturn(Single.just(FacilityPullResult.Success()))
    whenever(userSession.storeUser(expectedUser, registrationFacilityUuid))
        .thenReturn(Completable.complete())

    // when
    uiEvents.onNext(LoginPinAuthenticated("0000"))

    // then
    verify(facilitySync).pullWithResult()
    verify(requestLoginOtp).requestForUser(loginUserUuid)
    verify(userSession).storeUser(user = expectedUser, facilityUuid = registrationFacilityUuid)
    verify(screen).openHomeScreen()
  }
}
