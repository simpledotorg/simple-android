package org.simple.clinic.login.pin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.login.LoginResult
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class LoginPinScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<LoginPinScreen>()
  private val userSession = mock<UserSession>()
  private val localUser = PatientMocker.loggedInUser(pinDigest = "digest")

  private val uiEvents = PublishSubject.create<UiEvent>()

  private lateinit var controller: LoginPinScreenController

  @Before
  fun setUp() {
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    controller = LoginPinScreenController(userSession)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }

    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(localUser)))
    whenever(userSession.loggedInUserImmediate()).thenReturn(localUser)
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(localUser))
  }

  @Test
  fun `when screen starts, show phone number`() {
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(OngoingLoginEntry(uuid = UUID.randomUUID(), phoneNumber = "9977228833")))

    uiEvents.onNext(PinScreenCreated())

    verify(userSession).ongoingLoginEntry()
    verify(screen).showPhoneNumber("9977228833")
  }

  @Test
  fun `when PIN is submitted, request login otp and open home screen`() {
    val ongoingLoginEntry = OngoingLoginEntry(uuid = UUID.randomUUID(), phoneNumber = "9999232323")
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(ongoingLoginEntry))
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.requestLoginOtp()).thenReturn(Single.just(LoginResult.Success))

    uiEvents.onNext(LoginPinAuthenticated("0000"))

    verify(userSession).saveOngoingLoginEntry(OngoingLoginEntry(uuid = ongoingLoginEntry.uuid, phoneNumber = "9999232323", pin = "0000"))
    verify(userSession).requestLoginOtp()
    verify(screen).openHomeScreen()
  }

  @Test
  fun `if request otp api call throws any errors, show errors`() {
    val ongoingEntry = OngoingLoginEntry(uuid = UUID.randomUUID(), phoneNumber = "9999323232")
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.requestLoginOtp())
        .thenReturn(Single.just(LoginResult.NetworkError))
        .thenReturn(Single.just(LoginResult.UnexpectedError))

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
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.never<OngoingLoginEntry>())
    uiEvents.onNext(PinScreenCreated())

    verify(screen).submitWithPinDigest(localUser.pinDigest)
  }
}
