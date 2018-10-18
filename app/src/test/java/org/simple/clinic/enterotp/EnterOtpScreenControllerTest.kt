package org.simple.clinic.enterotp

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.LoginResult.NetworkError
import org.simple.clinic.login.LoginResult.ServerError
import org.simple.clinic.login.LoginResult.Success
import org.simple.clinic.login.LoginResult.UnexpectedError
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.sync.SyncScheduler
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

@RunWith(JUnitParamsRunner::class)
class EnterOtpScreenControllerTest {

  private lateinit var controller: EnterOtpScreenController
  private lateinit var userSession: UserSession
  private lateinit var screen: EnterOtpScreen

  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()
  private val syncScheduler = mock<SyncScheduler>()

  @Before
  fun setUp() {
    userSession = mock()
    screen = mock()
    whenever(syncScheduler.syncImmediately()).thenReturn(Completable.complete())

    controller = EnterOtpScreenController(userSession, syncScheduler)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when the screen is created, the logged in users phone number must be shown`() {
    val user = PatientMocker.loggedInUser(phone = "1111111111")
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(user))
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))

    uiEvents.onNext(ScreenCreated())

    verify(screen).showUserPhoneNumber("1111111111")
  }

  @Test
  fun `when back is pressed, the screen must be closed`() {
    uiEvents.onNext(EnterOtpBackClicked())

    verify(screen).goBack()
  }

  @Test
  @Parameters(
      "|true",
      "1|true",
      "11|true",
      "111|true",
      "1111|true",
      "11111|true",
      "111111|false"
  )
  fun `when an otp lesser than the required length is submitted, an error must be shown`(
      otp: String,
      shouldShowError: Boolean
  ) {
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(Success))

    uiEvents.onNext(EnterOtpSubmitted(otp))

    if (shouldShowError) {
      verify(screen).showIncorrectOtpError()
    } else {
      verify(screen, never()).showIncorrectOtpError()
    }
  }

  @Test
  @Parameters(
      "|false",
      "1|false",
      "11|false",
      "111|false",
      "1111|false",
      "11111|false",
      "111111|true"
  )
  fun `when an otp lesser than the required length is submitted, the login call must not be made`(
      otp: String,
      shouldLogin: Boolean
  ) {
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(Success))

    uiEvents.onNext(EnterOtpSubmitted(otp))

    if (shouldLogin) {
      verify(userSession).loginWithOtp(otp)
    } else {
      verify(userSession, never()).loginWithOtp(otp)
    }
  }

  @Test
  fun `when the otp is submitted, the login call must be made`() {
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(Success))

    uiEvents.onNext(EnterOtpSubmitted("111111"))

    verify(userSession).loginWithOtp("111111")
  }

  @Test
  fun `when the login call succeeds, the screen must be closed`() {
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(Success))

    uiEvents.onNext(EnterOtpSubmitted("111111"))

    verify(screen).goBack()
  }

  @Test
  fun `when the login call succeeds, the sync must be triggered`() {
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(Success))

    uiEvents.onNext(EnterOtpSubmitted("111111"))

    verify(syncScheduler).syncImmediately()
  }

  @Test
  fun `when the sync fails, the screen must close normally`() {
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(Success))
    whenever(syncScheduler.syncImmediately()).thenReturn(Completable.error(RuntimeException()))

    uiEvents.onNext(EnterOtpSubmitted("111111"))

    verify(screen).goBack()
  }

  @Test
  fun `when the login call fails unexpectedly, the generic error must be shown`() {
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(UnexpectedError))

    uiEvents.onNext(EnterOtpSubmitted("111111"))

    verify(screen).showUnexpectedError()
  }

  @Test
  fun `when the login call fails with a network error, the network error must be shown`() {
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(NetworkError))

    uiEvents.onNext(EnterOtpSubmitted("111111"))

    verify(screen).showNetworkError()
  }

  @Test
  fun `when the login call fails with a server error, the server error must be shown`() {
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(ServerError("Error")))

    uiEvents.onNext(EnterOtpSubmitted("111111"))

    verify(screen).showServerError("Error")
  }

  @Test
  fun `when the OTP text changes, the errors should be hidden`() {
    uiEvents.onNext(EnterOtpTextChanges("1"))
    uiEvents.onNext(EnterOtpTextChanges("11"))

    verify(screen, times(2)).hideError()
  }

  @Test
  fun `when the OTP changes and meets the otp length, the login call should be made`() {
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(Success))

    uiEvents.onNext(EnterOtpTextChanges("1111"))
    uiEvents.onNext(EnterOtpTextChanges("11111"))
    uiEvents.onNext(EnterOtpTextChanges("111111"))
    uiEvents.onNext(EnterOtpTextChanges("11111"))

    verify(userSession).loginWithOtp("111111")
  }

  @Test
  fun `when the login call is made, the network progress must be shown`() {
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.never<LoginResult>())

    uiEvents.onNext(EnterOtpSubmitted("111111"))

    verify(screen).showProgress()
    verify(screen, never()).hideProgress()
  }

  @Test
  @Parameters(method = "params for login call progress test")
  fun `when the login call succeeds or fails, the network progress must be hidden`(loginResult: LoginResult) {
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(loginResult))

    uiEvents.onNext(EnterOtpSubmitted("111111"))
    verify(screen).hideProgress()
  }

  fun `params for login call progress test`() = arrayOf<Any>(
      LoginResult.Success,
      LoginResult.NetworkError,
      LoginResult.ServerError("Test"),
      LoginResult.UnexpectedError
  )

  @Test
  @Parameters(
      "OTP_REQUESTED|OTP_REQUESTED|false",
      "OTP_REQUESTED|LOGGED_IN|true"
  )
  fun `when a user is verified for login in the background, the screen must be closed`(
      prevloggedInStatus: User.LoggedInStatus,
      curLoggedInStatus: User.LoggedInStatus,
      shouldCloseScreen: Boolean
  ) {
    val user = PatientMocker.loggedInUser(status = UserStatus.APPROVED_FOR_SYNCING, loggedInStatus = prevloggedInStatus)
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(user))
    whenever(userSession.loggedInUser()).thenReturn(
        Observable.just(
            Just(user),
            Just(user.copy(loggedInStatus = curLoggedInStatus)))
    )
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.complete())
    uiEvents.onNext(ScreenCreated())

    if (shouldCloseScreen) {
      Mockito.verify(screen).goBack()
    } else {
      Mockito.verify(screen, never()).goBack()
    }
  }
}
