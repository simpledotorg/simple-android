package org.simple.clinic.login.pin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val loginUserUuid = UUID.fromString("582099ce-e146-4aa5-8302-85dbbfa1b407")
  private val ongoingLoginEntry = OngoingLoginEntry(
      uuid = loginUserUuid,
      pinDigest = "",
      phoneNumber = ""
  )

  private val controller = LoginPinScreenController(
      userSession = userSession,
      requestLoginOtp = requestLoginOtp
  )

  @Before
  fun setUp() {
    whenever(requestLoginOtp.requestForUser(loginUserUuid))
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

    val enteredPin = "0000"
    uiEvents.onNext(LoginPinAuthenticated(enteredPin))
    uiEvents.onNext(LoginPinAuthenticated(enteredPin))

    verify(screen).showNetworkError()
    verify(screen).showUnexpectedError()
  }

  @Test
  fun `when back is clicked, the local logged in user must be cleared before moving back`() {
    whenever(userSession.clearLoggedInUser()).thenReturn(Completable.complete())
    whenever(userSession.clearOngoingLoginEntry()).thenReturn(Completable.complete())

    uiEvents.onNext(PinBackClicked())
    verify(userSession).clearLoggedInUser()
  }

  @Test
  fun `when back is clicked, the local ongoing login entry must be cleared`() {
    whenever(userSession.clearLoggedInUser()).thenReturn(Completable.complete())
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
}
