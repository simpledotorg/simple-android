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
import org.simple.clinic.login.LoginResult
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

  private val controller = LoginPinScreenController(
      userSession = userSession,
      requestLoginOtp = requestLoginOtp
  )

  @Before
  fun setUp() {
    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts, show phone number`() {
    whenever(userSession.ongoingLoginEntry())
        .thenReturn(Single.just(OngoingLoginEntry(uuid = UUID.randomUUID(), phoneNumber = "9977228833", pinDigest = "")))

    uiEvents.onNext(PinScreenCreated())

    verify(screen).showPhoneNumber("9977228833")
  }

  @Test
  fun `when PIN is submitted, request login otp and open home screen`() {
    val loginUserUuid = UUID.fromString("582099ce-e146-4aa5-8302-85dbbfa1b407")
    val ongoingLoginEntry = OngoingLoginEntry(uuid = loginUserUuid, phoneNumber = "9999232323")
    whenever(userSession.ongoingLoginEntry())
        .thenReturn(Single.just(ongoingLoginEntry))
    whenever(userSession.saveOngoingLoginEntry(any()))
        .thenReturn(Completable.complete())
    whenever(requestLoginOtp.requestForUser(loginUserUuid))
        .thenReturn(Single.just(RequestLoginOtp.Result.Success))

    uiEvents.onNext(LoginPinAuthenticated("0000"))

    verify(userSession).saveOngoingLoginEntry(OngoingLoginEntry(uuid = ongoingLoginEntry.uuid, phoneNumber = "9999232323", pin = "0000"))
    verify(requestLoginOtp).requestForUser(loginUserUuid)
    verify(screen).openHomeScreen()
  }

  @Test
  fun `if request otp api call throws any errors, show errors`() {
    val loginUserUuid = UUID.fromString("582099ce-e146-4aa5-8302-85dbbfa1b407")
    val ongoingEntry = OngoingLoginEntry(uuid = loginUserUuid, phoneNumber = "9999323232")
    whenever(userSession.ongoingLoginEntry())
        .thenReturn(Single.just(ongoingEntry))
    whenever(userSession.saveOngoingLoginEntry(any()))
        .thenReturn(Completable.complete())
    whenever(requestLoginOtp.requestForUser(loginUserUuid))
        .thenReturn(Single.just(RequestLoginOtp.Result.NetworkError))
        .thenReturn(Single.just(RequestLoginOtp.Result.OtherError(RuntimeException())))

    uiEvents.onNext(LoginPinAuthenticated("0000"))
    uiEvents.onNext(LoginPinAuthenticated("0000"))

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
    val ongoingLoginEntry = OngoingLoginEntry(uuid = UUID.randomUUID(), pinDigest = "digest", phoneNumber = "")
    whenever(userSession.ongoingLoginEntry())
        .thenReturn(Single.just<OngoingLoginEntry>(ongoingLoginEntry))

    uiEvents.onNext(PinScreenCreated())

    verify(screen).submitWithPinDigest(ongoingLoginEntry.pinDigest!!)
  }
}
