package org.simple.clinic.enterotp

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.LoginResult.NetworkError
import org.simple.clinic.login.LoginResult.ServerError
import org.simple.clinic.login.LoginResult.Success
import org.simple.clinic.login.LoginResult.UnexpectedError
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.Just
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

@RunWith(JUnitParamsRunner::class)
class EnterOtpScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private lateinit var controller: EnterOtpScreenController
  private lateinit var userSession: UserSession
  private lateinit var screen: EnterOtpScreen

  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()

  @Before
  fun setUp() {
    userSession = mock()
    screen = mock()

    controller = EnterOtpScreenController(userSession)

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
  fun `when the login call fails unexpectedly, the PIN should be cleared`() {
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(UnexpectedError))

    uiEvents.onNext(EnterOtpSubmitted("111111"))

    verify(screen).clearPin()
  }

  @Test
  fun `when the login call fails with a network error, the PIN should be cleared`() {
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(NetworkError))

    uiEvents.onNext(EnterOtpSubmitted("111111"))

    verify(screen).clearPin()
  }

  @Test
  fun `when the login call fails with a server error, the PIN should be cleared`() {
    whenever(userSession.loginWithOtp(any())).thenReturn(Single.just(ServerError("Error")))

    uiEvents.onNext(EnterOtpSubmitted("111111"))

    verify(screen).clearPin()
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

  @Suppress("Unused")
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
    val user = PatientMocker.loggedInUser(status = UserStatus.ApprovedForSyncing, loggedInStatus = prevloggedInStatus)
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

  @Test
  fun `when resend sms is clicked, the request otp flow should be triggered`() {
    var otpRequested = false
    whenever(userSession.requestLoginOtp())
        .thenReturn(Single.just(LoginResult.Success as LoginResult).doOnSuccess { otpRequested = true })

    uiEvents.onNext(EnterOtpResendSmsClicked())

    assertThat(otpRequested).isTrue()
  }

  @Test
  fun `when the resend sms call is made, the progress must be shown`() {
    whenever(userSession.requestLoginOtp()).thenReturn(Single.just(LoginResult.Success))

    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).showProgress()
  }

  @Test
  fun `when the resend sms call is made, any error must be hidden`() {
    whenever(userSession.requestLoginOtp()).thenReturn(Single.just(LoginResult.Success))

    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).hideError()
  }

  @Test
  @Parameters(method = "params for hiding progress on request otp")
  fun `when the resend sms call completes, the progress must be hidden`(
      result: Single<LoginResult>
  ) {
    whenever(userSession.requestLoginOtp()).thenReturn(result)

    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).hideProgress()
  }


  @Suppress("Unused")
  private fun `params for hiding progress on request otp`(): List<List<Any>> {
    return listOf(
        listOf(Single.just(LoginResult.Success)),
        listOf(Single.just(LoginResult.NetworkError)),
        listOf(Single.just(LoginResult.ServerError("error"))),
        listOf(Single.just(LoginResult.UnexpectedError))
    )
  }

  @Test
  @Parameters(method = "params for showing SMS sent message")
  fun `when the resend sms call succeeds, the sms sent message must be shown`(
      result: Single<LoginResult>,
      shouldShowMessage: Boolean
  ) {
    whenever(userSession.requestLoginOtp()).thenReturn(result)

    uiEvents.onNext(EnterOtpResendSmsClicked())

    if (shouldShowMessage) {
      verify(screen).showSmsSentMessage()
    } else {
      verify(screen, never()).showSmsSentMessage()
    }
  }

  @Suppress("Unused")
  private fun `params for showing SMS sent message`(): List<List<Any>> {
    return listOf(
        listOf(Single.just(LoginResult.Success), true),
        listOf(Single.just(LoginResult.NetworkError), false),
        listOf(Single.just(LoginResult.ServerError("error")), false),
        listOf(Single.just(LoginResult.UnexpectedError), false)
    )
  }

  @Test
  @Parameters(method = "params for show error on request otp")
  fun `when the resend sms call completes, the error must be shown`(
      result: Single<LoginResult>,
      verification: (Ui) -> Unit
  ) {
    whenever(userSession.requestLoginOtp()).thenReturn(result)

    uiEvents.onNext(EnterOtpResendSmsClicked())

    verification(screen)
  }

  @Suppress("Unused")
  private fun `params for show error on request otp`(): List<List<Any>> {
    return listOf(
        listOf<Any>(
            Single.just(LoginResult.Success),
            { screen: Ui ->
              verify(screen, never()).showNetworkError()
              verify(screen, never()).showServerError(any())
              verify(screen, never()).showUnexpectedError()
            }),
        listOf<Any>(
            Single.just(LoginResult.UnexpectedError),
            { screen: Ui -> verify(screen).showUnexpectedError() }),
        listOf<Any>(
            Single.just(LoginResult.NetworkError),
            { screen: Ui -> verify(screen).showNetworkError() }),
        listOf<Any>(
            Single.just(LoginResult.ServerError("Error 1")),
            { screen: Ui -> verify(screen).showServerError("Error 1") }),
        listOf<Any>(
            Single.just(LoginResult.NetworkError),
            { screen: Ui -> verify(screen).showNetworkError() })
    )
  }

  @Test
  @Parameters(method = "params for clear PIN on request otp")
  fun `when the resend sms call fails, the PIN must be cleared`(result: LoginResult) {
    whenever(userSession.requestLoginOtp()).thenReturn(Single.just(result))

    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).clearPin()
  }

  @Suppress("Unused")
  private fun `params for clear PIN on request otp`(): List<Any> {
    return listOf(
        LoginResult.UnexpectedError,
        LoginResult.ServerError("Error 1"),
        LoginResult.ServerError("Error 2"),
        LoginResult.NetworkError)
  }
}
