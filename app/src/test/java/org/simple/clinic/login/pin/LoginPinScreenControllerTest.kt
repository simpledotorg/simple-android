package org.simple.clinic.login.pin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.login.LoginConfig
import org.simple.clinic.login.LoginOtpSmsListener
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.applock.PasswordHasher
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.sync.SyncScheduler
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class LoginPinScreenControllerTest {

  private val screen = mock<LoginPinScreen>()
  private val userSession = mock<UserSession>()
  private val loginSmsListener = mock<LoginOtpSmsListener>()
  private val passwordHasher = mock<PasswordHasher>()
  private val localUser = PatientMocker.loggedInUser(pinDigest = "digest")

  private val syncScheduler = mock<SyncScheduler>()
  private val uiEvents = PublishSubject.create<UiEvent>()

  private val loginConfigEmitter = PublishSubject.create<LoginConfig>()
  private lateinit var controller: LoginPinScreenController


  @Before
  fun setUp() {
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    controller = LoginPinScreenController(userSession, syncScheduler, loginConfigEmitter.firstOrError(), loginSmsListener, passwordHasher)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }

    whenever(syncScheduler.syncImmediately()).thenReturn(Completable.complete())
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(localUser)))
    whenever(userSession.loggedInUserImmediate()).thenReturn(localUser)
  }

  @Test
  fun `when screen starts, show phone number`() {
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(OngoingLoginEntry(userId = UUID.randomUUID(), phoneNumber = "123")))

    uiEvents.onNext(PinScreenCreated())

    verify(userSession).ongoingLoginEntry()
    verify(screen).showPhoneNumber("123")
  }

  @Test
  fun `if pin is not empty, and submit is clicked, make login api call, and open home screen`() {
    val ongoingEntry = OngoingLoginEntry(userId = UUID.randomUUID(), phoneNumber = "99999")
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(LoginResult.Success()))

    loginConfigEmitter.onNext(LoginConfig(isOtpLoginFlowEnabled = false))
    uiEvents.onNext(PinTextChanged("0000"))
    uiEvents.onNext(LoginPinOtpReceived("0000"))
    uiEvents.onNext(PinSubmitClicked())

    val inOrder = inOrder(userSession, screen)
    inOrder.verify(userSession).loginWithOtp("0000")
    inOrder.verify(userSession).ongoingLoginEntry()
    inOrder.verify(userSession).saveOngoingLoginEntry(OngoingLoginEntry(userId = ongoingEntry.userId, phoneNumber = "99999", pin = "0000"))
    inOrder.verify(screen).showProgressBar()
    inOrder.verify(screen).openHomeScreen()
  }

  @Test
  fun `if login api call throws any errors, show errors`() {
    val ongoingEntry = OngoingLoginEntry(userId = UUID.randomUUID(), phoneNumber = "99999")
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.loginWithOtp(any()))
        .thenReturn(Single.just(LoginResult.NetworkError()))
        .thenReturn(Single.just(LoginResult.ServerError("Server error")))
        .thenReturn(Single.just(LoginResult.UnexpectedError()))

    loginConfigEmitter.onNext(LoginConfig(isOtpLoginFlowEnabled = false))
    uiEvents.onNext(PinTextChanged("0000"))
    uiEvents.onNext(LoginPinOtpReceived("0000"))
    uiEvents.onNext(PinSubmitClicked())
    uiEvents.onNext(PinSubmitClicked())
    uiEvents.onNext(PinSubmitClicked())

    verify(screen).showNetworkError()
    verify(screen).showServerError("Server error")
    verify(screen).showUnexpectedError()
  }

  @Test
  @Parameters(method = "values for data sync")
  fun `data should only be synced when login succeeds`(loginResult: LoginResult, shouldSync: Boolean) {
    whenever(syncScheduler.syncImmediately()).thenReturn(Completable.complete())

    val ongoingEntry = OngoingLoginEntry(userId = UUID.randomUUID(), phoneNumber = "99999")
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(loginResult))

    loginConfigEmitter.onNext(LoginConfig(isOtpLoginFlowEnabled = false))
    uiEvents.onNext(PinTextChanged("0000"))
    uiEvents.onNext(LoginPinOtpReceived("0000"))
    uiEvents.onNext(PinSubmitClicked())

    if (shouldSync) {
      verify(syncScheduler).syncImmediately()
    } else {
      verify(syncScheduler, never()).syncImmediately()
    }
  }

  @Test
  fun `when new login flow is enabled, if pin is incorrect and submit is clicked, show an error`() {
    whenever(passwordHasher.compare(any(), any())).thenReturn(Single.just(PasswordHasher.ComparisonResult.DIFFERENT))
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(OngoingLoginEntry(userId = UUID.randomUUID())))
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())

    loginConfigEmitter.onNext(LoginConfig(isOtpLoginFlowEnabled = true))
    uiEvents.onNext(PinTextChanged("1234"))
    uiEvents.onNext(PinSubmitClicked())

    verify(screen).showIncorrectPinError()
    verify(loginSmsListener, never()).listenForLoginOtp()
  }

  @Test
  fun `when new login flow is enabled any existing errors should be hidden when the user starts typing again`() {
    loginConfigEmitter.onNext(LoginConfig(isOtpLoginFlowEnabled = true))
    uiEvents.onNext(PinTextChanged("0"))
    verify(screen).hideError()
  }

  @Test
  fun `when new login flow is enabled, and PIN is submitted, request login otp and open home screen`() {
    val ongoingLoginEntry = OngoingLoginEntry(userId = UUID.randomUUID(), phoneNumber = "9999")
    whenever(passwordHasher.compare(any(), any())).thenReturn(Single.just(PasswordHasher.ComparisonResult.SAME))
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(ongoingLoginEntry))
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.requestLoginOtp()).thenReturn(Single.just(LoginResult.Success()))
    whenever(loginSmsListener.listenForLoginOtp()).thenReturn(Completable.complete())

    loginConfigEmitter.onNext(LoginConfig(isOtpLoginFlowEnabled = true))
    uiEvents.onNext(PinTextChanged("0000"))
    uiEvents.onNext(PinSubmitClicked())

    verify(userSession).saveOngoingLoginEntry(OngoingLoginEntry(userId = ongoingLoginEntry.userId, phoneNumber = "9999", pin = "0000"))
    verify(loginSmsListener).listenForLoginOtp()
    verify(userSession).requestLoginOtp()
    verify(screen).showProgressBar()
    verify(screen).openHomeScreen()
  }

  @Test
  fun `when new login flow is enabled, if pin is not empty and submit is clicked, if the sms listener fails, then show the error screen`() {
    val ongoingLoginEntry = OngoingLoginEntry(userId = UUID.randomUUID(), phoneNumber = "9999")
    whenever(passwordHasher.compare(any(), any())).thenReturn(Single.just(PasswordHasher.ComparisonResult.SAME))
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(ongoingLoginEntry))
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.requestLoginOtp()).thenReturn(Single.just(LoginResult.Success()))
    whenever(loginSmsListener.listenForLoginOtp()).thenReturn(Completable.error(RuntimeException()))

    loginConfigEmitter.onNext(LoginConfig(isOtpLoginFlowEnabled = true))
    uiEvents.onNext(PinTextChanged("0000"))
    uiEvents.onNext(PinSubmitClicked())

    verify(userSession).saveOngoingLoginEntry(OngoingLoginEntry(userId = ongoingLoginEntry.userId, phoneNumber = "9999", pin = "0000"))
    verify(screen).showUnexpectedError()
  }

  @Test
  fun `when new login flow is enabled, if request api call throws any errors, show errors`() {
    val ongoingEntry = OngoingLoginEntry(userId = UUID.randomUUID(), phoneNumber = "99999")
    whenever(passwordHasher.compare(any(), any())).thenReturn(Single.just(PasswordHasher.ComparisonResult.SAME))
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())
    whenever(loginSmsListener.listenForLoginOtp()).thenReturn(Completable.complete())
    whenever(userSession.requestLoginOtp())
        .thenReturn(Single.just(LoginResult.NetworkError()))
        .thenReturn(Single.just(LoginResult.UnexpectedError()))

    loginConfigEmitter.onNext(LoginConfig(isOtpLoginFlowEnabled = true))
    uiEvents.onNext(PinTextChanged("0000"))
    uiEvents.onNext(PinSubmitClicked())
    uiEvents.onNext(PinSubmitClicked())

    verify(screen).showNetworkError()
    verify(screen).showUnexpectedError()
  }

  fun `values for data sync`(): Array<Any> {
    return arrayOf(
        arrayOf(LoginResult.Success(), true),
        arrayOf(LoginResult.ServerError("some error"), false),
        arrayOf(LoginResult.UnexpectedError(), false),
        arrayOf(LoginResult.NetworkError(), false)
    )
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
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PinBackClicked())
    verify(userSession).clearOngoingLoginEntry()
  }
}
