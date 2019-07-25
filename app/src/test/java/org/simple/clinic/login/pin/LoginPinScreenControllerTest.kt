package org.simple.clinic.login.pin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
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
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
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
      phoneNumber = ""
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
  fun `when PIN is submitted, request login otp and open home screen`() {
    whenever(userSession.ongoingLoginEntry())
        .thenReturn(Single.just(ongoingLoginEntry))
    whenever(userSession.saveOngoingLoginEntry(any()))
        .thenReturn(Completable.complete())
    whenever(requestLoginOtp.requestForUser(loginUserUuid))
        .thenReturn(Single.just(RequestLoginOtp.Result.Success))
    whenever(facilitySync.pullWithResult())
        .thenReturn(Single.just(FacilityPullResult.Success()))

    uiEvents.onNext(LoginPinAuthenticated("0000"))

    verify(requestLoginOtp).requestForUser(loginUserUuid)
    verify(screen).openHomeScreen()
  }

  @Test
  fun `if request otp api call throws any errors, show errors`() {
    whenever(userSession.ongoingLoginEntry())
        .thenReturn(Single.just(ongoingLoginEntry))
    whenever(userSession.saveOngoingLoginEntry(any()))
        .thenReturn(Completable.complete())
    whenever(requestLoginOtp.requestForUser(loginUserUuid))
        .thenReturn(Single.just(RequestLoginOtp.Result.NetworkError))
        .thenReturn(Single.just(RequestLoginOtp.Result.OtherError(RuntimeException())))
    whenever(facilitySync.pullWithResult())
        .thenReturn(Single.just(FacilityPullResult.Success()))

    val enteredPin = "0000"
    uiEvents.onNext(LoginPinAuthenticated(enteredPin))
    uiEvents.onNext(LoginPinAuthenticated(enteredPin))

    verify(screen).showNetworkError()
    verify(screen).showUnexpectedError()
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
  fun `when PIN is submitted, sync facilities, request login OTP and open the home screen`() {
    // given
    whenever(userSession.ongoingLoginEntry())
        .thenReturn(Single.just(ongoingLoginEntry))
    whenever(userSession.saveOngoingLoginEntry(any()))
        .thenReturn(Completable.complete())
    whenever(requestLoginOtp.requestForUser(loginUserUuid))
        .thenReturn(Single.just(RequestLoginOtp.Result.Success))
    whenever(facilitySync.pullWithResult())
        .thenReturn(Single.just(FacilityPullResult.Success()))

    // when
    uiEvents.onNext(LoginPinAuthenticated("0000"))

    // then
    verify(facilitySync).pullWithResult()
    verify(requestLoginOtp).requestForUser(loginUserUuid)
    verify(screen).openHomeScreen()
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
    uiEvents.onNext(LoginPinAuthenticated("0000"))

    // then
    verify(facilitySync, times(2)).pullWithResult()
    verify(requestLoginOtp, never()).requestForUser(loginUserUuid)
    verify(screen).showNetworkError()
    verify(screen).showUnexpectedError()
  }
}
