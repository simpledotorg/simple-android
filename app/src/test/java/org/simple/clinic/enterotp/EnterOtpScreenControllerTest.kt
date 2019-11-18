package org.simple.clinic.enterotp

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
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
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.LoginResult.NetworkError
import org.simple.clinic.login.LoginResult.ServerError
import org.simple.clinic.login.LoginResult.Success
import org.simple.clinic.login.LoginResult.UnexpectedError
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.login.LoginUserWithOtp
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.RequestLoginOtp
import org.simple.clinic.user.RequestLoginOtp.Result
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class EnterOtpScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val userSession = mock<UserSession>()
  private val screen = mock<EnterOtpScreen>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val requestLoginOtp = mock<RequestLoginOtp>()
  private val loginUserWithOtp = mock<LoginUserWithOtp>()

  private val otp = "111111"
  private val pin = "1234"
  private val phoneNumber = "1234567890"
  private val loggedInUserUuid = UUID.fromString("13e563ba-a148-4b5d-838d-e38d42dca72c")
  private val registrationFacilityUuid = UUID.fromString("f33bfc01-f595-42ce-8ad8-b70150ccbde2")
  private val user = PatientMocker.loggedInUser(uuid = loggedInUserUuid, phone = phoneNumber)
  private val ongoingLoginEntry = OngoingLoginEntry(
      uuid = loggedInUserUuid,
      phoneNumber = phoneNumber,
      pin = pin,
      fullName = user.fullName,
      pinDigest = user.pinDigest,
      registrationFacilityUuid = registrationFacilityUuid,
      status = user.status,
      createdAt = user.createdAt,
      updatedAt = user.updatedAt
  )

  private val controller = EnterOtpScreenController(userSession, requestLoginOtp, loginUserWithOtp)

  @Before
  fun setUp() {
    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when the screen is created, the logged in users phone number must be shown`() {
    whenever(userSession.requireLoggedInUser()).doReturn(Observable.just(user))
    whenever(userSession.loggedInUser()).doReturn(Observable.just<Optional<User>>(Just(user)))

    uiEvents.onNext(ScreenCreated())

    verify(screen).showUserPhoneNumber(phoneNumber)
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
    whenever(userSession.ongoingLoginEntry()).doReturn(Single.just(ongoingLoginEntry))
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    uiEvents.onNext(EnterOtpSubmitted(otp))

    if (shouldShowError) {
      verifyZeroInteractions(loginUserWithOtp)
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
    whenever(userSession.ongoingLoginEntry()).doReturn(Single.just(ongoingLoginEntry))
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    uiEvents.onNext(EnterOtpSubmitted(otp))

    if (shouldLogin) {
      verify(loginUserWithOtp).loginWithOtp(phoneNumber, pin, otp)
    } else {
      verifyZeroInteractions(loginUserWithOtp)
      verify(loginUserWithOtp, never()).loginWithOtp(phoneNumber, pin, otp)
    }
  }

  @Test
  fun `when the otp is submitted, the login call must be made`() {
    whenever(userSession.ongoingLoginEntry()).doReturn(Single.just(ongoingLoginEntry))
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(loginUserWithOtp).loginWithOtp(phoneNumber, pin, otp)
  }

  @Test
  fun `when the login call succeeds, the screen must be closed`() {
    whenever(userSession.ongoingLoginEntry()).doReturn(Single.just(ongoingLoginEntry))
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).goBack()
  }

  @Test
  fun `when the login call fails unexpectedly, the generic error must be shown`() {
    whenever(userSession.ongoingLoginEntry()).doReturn(Single.just(ongoingLoginEntry))
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(UnexpectedError))

    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).showUnexpectedError()
  }

  @Test
  fun `when the login call fails with a network error, the network error must be shown`() {
    whenever(userSession.ongoingLoginEntry()).doReturn(Single.just(ongoingLoginEntry))
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(NetworkError))

    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).showNetworkError()
  }

  @Test
  fun `when the login call fails with a server error, the server error must be shown`() {
    whenever(userSession.ongoingLoginEntry()).doReturn(Single.just(ongoingLoginEntry))
    val errorMessage = "Error"
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(ServerError(errorMessage)))

    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).showServerError(errorMessage)
  }

  @Test
  fun `when the login call fails unexpectedly, the PIN should be cleared`() {
    whenever(userSession.ongoingLoginEntry()).doReturn(Single.just(ongoingLoginEntry))
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(UnexpectedError))

    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).clearPin()
  }

  @Test
  fun `when the login call fails with a network error, the PIN should be cleared`() {
    whenever(userSession.ongoingLoginEntry()).doReturn(Single.just(ongoingLoginEntry))
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(NetworkError))

    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).clearPin()
  }

  @Test
  fun `when the login call fails with a server error, the PIN should be cleared`() {
    whenever(userSession.ongoingLoginEntry()).doReturn(Single.just(ongoingLoginEntry))
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(ServerError("Error")))

    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).clearPin()
  }

  @Test
  fun `when the OTP changes and meets the otp length, the login call should be made`() {
    whenever(userSession.ongoingLoginEntry()).doReturn(Single.just(ongoingLoginEntry))
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    uiEvents.onNext(EnterOtpTextChanges("1111"))
    uiEvents.onNext(EnterOtpTextChanges("11111"))
    uiEvents.onNext(EnterOtpTextChanges("111111"))
    uiEvents.onNext(EnterOtpTextChanges("11111"))

    verify(loginUserWithOtp).loginWithOtp(phoneNumber, pin, "111111")
  }

  @Test
  fun `when the login call is made, the network progress must be shown`() {
    whenever(userSession.ongoingLoginEntry()).doReturn(Single.just(ongoingLoginEntry))
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.never<LoginResult>())

    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).showProgress()
    verify(screen, never()).hideProgress()
  }

  @Test
  @Parameters(method = "params for login call progress test")
  fun `when the login call succeeds or fails, the network progress must be hidden`(loginResult: LoginResult) {
    whenever(userSession.ongoingLoginEntry()).doReturn(Single.just(ongoingLoginEntry))
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just(loginResult))

    uiEvents.onNext(EnterOtpSubmitted(otp))
    verify(screen).hideProgress()
  }

  @Suppress("Unused")
  fun `params for login call progress test`() = arrayOf<Any>(
      Success,
      NetworkError,
      ServerError("Test"),
      UnexpectedError
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
    whenever(userSession.requireLoggedInUser()).doReturn(Observable.just(user))
    whenever(userSession.loggedInUser()).doReturn(
        Observable.just<Optional<User>>(
            Just(user),
            Just(user.copy(loggedInStatus = curLoggedInStatus)))
    )
    whenever(userSession.refreshLoggedInUser()).doReturn(Completable.complete())
    uiEvents.onNext(ScreenCreated())

    if (shouldCloseScreen) {
      verify(screen).goBack()
    } else {
      verify(screen, never()).goBack()
    }
  }

  @Test
  fun `when resend sms is clicked, the request otp flow should be triggered`() {
    var otpRequested = false
    val requestOtpSingle = Single
        .just(Result.Success as Result)
        .doOnSubscribe { otpRequested = true }
    whenever(requestLoginOtp.requestForUser(loggedInUserUuid))
        .doReturn(requestOtpSingle)
    whenever(userSession.requireLoggedInUser())
        .doReturn(Observable.just(PatientMocker.loggedInUser(uuid = loggedInUserUuid)))

    uiEvents.onNext(EnterOtpResendSmsClicked())

    assertThat(otpRequested).isTrue()
  }

  @Test
  fun `when the resend sms call is made, the progress must be shown`() {
    whenever(requestLoginOtp.requestForUser(loggedInUserUuid))
        .doReturn(Single.just(Result.Success as Result))
    whenever(userSession.requireLoggedInUser())
        .doReturn(Observable.just(PatientMocker.loggedInUser(uuid = loggedInUserUuid)))

    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).showProgress()
  }

  @Test
  fun `when the resend sms call is made, any error must be hidden`() {
    whenever(requestLoginOtp.requestForUser(loggedInUserUuid))
        .doReturn(Single.just(Result.Success as Result))
    whenever(userSession.requireLoggedInUser())
        .doReturn(Observable.just(PatientMocker.loggedInUser(uuid = loggedInUserUuid)))

    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).hideError()
  }

  @Test
  @Parameters(method = "params for hiding progress on request otp")
  fun `when the resend sms call completes, the progress must be hidden`(
      result: Result
  ) {
    whenever(requestLoginOtp.requestForUser(loggedInUserUuid))
        .doReturn(Single.just(result))
    whenever(userSession.requireLoggedInUser())
        .doReturn(Observable.just(PatientMocker.loggedInUser(uuid = loggedInUserUuid)))

    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).hideProgress()
  }


  @Suppress("Unused")
  private fun `params for hiding progress on request otp`(): List<Result> {
    return listOf(
        Result.Success,
        Result.OtherError(RuntimeException()),
        Result.NetworkError,
        Result.ServerError(500)
    )
  }

  @Test
  @Parameters(method = "params for showing SMS sent message")
  fun `when the resend sms call succeeds, the sms sent message must be shown`(params: SmsSendShowMessageTestParams) {
    whenever(requestLoginOtp.requestForUser(loggedInUserUuid))
        .doReturn(Single.just(params.result))
    whenever(userSession.requireLoggedInUser())
        .doReturn(Observable.just(PatientMocker.loggedInUser(uuid = loggedInUserUuid)))

    uiEvents.onNext(EnterOtpResendSmsClicked())

    if (params.shouldShowMessage) {
      verify(screen).showSmsSentMessage()
    } else {
      verify(screen, never()).showSmsSentMessage()
    }
  }

  data class SmsSendShowMessageTestParams(val result: Result, val shouldShowMessage: Boolean)

  @Suppress("Unused")
  private fun `params for showing SMS sent message`(): List<SmsSendShowMessageTestParams> {
    return listOf(
        SmsSendShowMessageTestParams(result = Result.Success, shouldShowMessage = true),
        SmsSendShowMessageTestParams(result = Result.NetworkError, shouldShowMessage = false),
        SmsSendShowMessageTestParams(result = Result.OtherError(RuntimeException()), shouldShowMessage = false),
        SmsSendShowMessageTestParams(result = Result.ServerError(400), shouldShowMessage = false)
    )
  }

  @Test
  @Parameters(method = "params for show error on request otp")
  fun `when the resend sms call completes, the error must be shown`(params: SmsSendShowErrorParams) {
    whenever(requestLoginOtp.requestForUser(loggedInUserUuid))
        .doReturn(Single.just(params.result))
    whenever(userSession.requireLoggedInUser())
        .doReturn(Observable.just(PatientMocker.loggedInUser(uuid = loggedInUserUuid)))

    uiEvents.onNext(EnterOtpResendSmsClicked())

    params.verifications.invoke(screen)
  }

  data class SmsSendShowErrorParams(
      val result: Result,
      // FIXME 25-Jul-19: This should be broken into separate tests
      val verifications: (Ui) -> Unit
  )

  @Suppress("Unused")
  private fun `params for show error on request otp`(): List<SmsSendShowErrorParams> {
    return listOf(
        SmsSendShowErrorParams(
            result = Result.Success,
            verifications = { screen: Ui ->
              verify(screen, never()).showNetworkError()
              verify(screen, never()).showServerError(any())
              verify(screen, never()).showUnexpectedError()
            }
        ),
        SmsSendShowErrorParams(
            result = Result.NetworkError,
            verifications = { screen: Ui -> verify(screen).showNetworkError() }
        ),
        SmsSendShowErrorParams(
            result = Result.OtherError(RuntimeException()),
            verifications = { screen: Ui -> verify(screen).showUnexpectedError() }
        ),
        SmsSendShowErrorParams(
            result = Result.ServerError(400),
            verifications = { screen: Ui -> verify(screen).showUnexpectedError() }
        )
    )
  }

  @Test
  @Parameters(method = "params for clear PIN on request otp")
  fun `when the resend sms call fails, the PIN must be cleared`(result: Result) {
    whenever(requestLoginOtp.requestForUser(loggedInUserUuid))
        .doReturn(Single.just(result))
    whenever(userSession.requireLoggedInUser())
        .doReturn(Observable.just(PatientMocker.loggedInUser(uuid = loggedInUserUuid)))

    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).clearPin()
  }

  @Suppress("Unused")
  private fun `params for clear PIN on request otp`(): List<Result> {
    return listOf(
        Result.OtherError(RuntimeException()),
        Result.ServerError(400),
        Result.NetworkError)
  }

  @Test
  fun `when the login call succeeds, the ongoing login entry must be cleared`() {
    whenever(userSession.ongoingLoginEntry()).doReturn(Single.just(ongoingLoginEntry))
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(userSession).clearOngoingLoginEntry()
  }

  @Test
  fun `when the login call fails, the ongoing login entry must not be cleared`() {
    whenever(userSession.ongoingLoginEntry()).doReturn(Single.just(ongoingLoginEntry))
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(NetworkError))

    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(userSession, never()).clearOngoingLoginEntry()
  }
}
