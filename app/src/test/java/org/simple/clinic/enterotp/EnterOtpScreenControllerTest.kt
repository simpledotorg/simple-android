package org.simple.clinic.enterotp

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.LoginResult.NetworkError
import org.simple.clinic.login.LoginResult.ServerError
import org.simple.clinic.login.LoginResult.Success
import org.simple.clinic.login.LoginResult.UnexpectedError
import org.simple.clinic.login.LoginUserWithOtp
import org.simple.clinic.login.activateuser.ActivateUser
import org.simple.clinic.sync.DataSync
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.OngoingLoginEntryRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.OTP_REQUESTED
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus.ApprovedForSyncing
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class EnterOtpScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val userSession = mock<UserSession>()
  private val screen = mock<EnterOtpScreen>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val activateUser = mock<ActivateUser>()
  private val loginUserWithOtp = mock<LoginUserWithOtp>()
  private val ongoingLoginEntryRepository = mock<OngoingLoginEntryRepository>()
  private val dataSync = mock<DataSync>()

  private val otp = "111111"
  private val pin = "1234"
  private val phoneNumber = "1234567890"
  private val loggedInUserUuid = UUID.fromString("13e563ba-a148-4b5d-838d-e38d42dca72c")
  private val registrationFacilityUuid = UUID.fromString("f33bfc01-f595-42ce-8ad8-b70150ccbde2")
  private val user = TestData.loggedInUser(
      uuid = loggedInUserUuid,
      phone = phoneNumber,
      status = ApprovedForSyncing,
      loggedInStatus = OTP_REQUESTED
  )
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
  private val userPayload = TestData.loggedInUserPayload(
      uuid = loggedInUserUuid,
      name = user.fullName,
      phone = user.phoneNumber,
      pinDigest = user.pinDigest,
      registrationFacilityUuid = registrationFacilityUuid,
      status = user.status,
      createdAt = user.createdAt,
      updatedAt = user.updatedAt
  )

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when the screen is created, the logged in users phone number must be shown`() {
    setupController()

    verify(screen).showUserPhoneNumber(phoneNumber)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when back is pressed, the screen must be closed`() {
    setupController()
    uiEvents.onNext(EnterOtpBackClicked())

    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).goBack()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when an otp of the right length is submitted, an error must not be shown`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen, never()).showIncorrectOtpError()
    verify(screen).hideProgress()
    verify(screen).goBack()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when an otp lesser than the required length is submitted, an error must be shown`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted("11111"))

    verifyZeroInteractions(loginUserWithOtp)
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showIncorrectOtpError()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when an otp of the required length is submitted, the login call must be made`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(loginUserWithOtp).loginWithOtp(phoneNumber, pin, otp)
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).goBack()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when an otp lesser than the required length is submitted, the login call must not be made`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted("11111"))

    verifyZeroInteractions(loginUserWithOtp)
    verify(loginUserWithOtp, never()).loginWithOtp(phoneNumber, pin, otp)
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showIncorrectOtpError()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the otp is submitted, the login call must be made`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(loginUserWithOtp).loginWithOtp(phoneNumber, pin, otp)
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).goBack()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the login call succeeds, the screen must be closed`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).goBack()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the login call succeeds, a complete sync must be triggered`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(dataSync).fireAndForgetSync()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).goBack()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the login call fails unexpectedly, the generic error must be shown`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(UnexpectedError))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).showUnexpectedError()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
    verify(dataSync, never()).fireAndForgetSync()
  }

  @Test
  fun `when the login call fails with a network error, the network error must be shown`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(NetworkError))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).showNetworkError()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
    verify(dataSync, never()).fireAndForgetSync()
  }

  @Test
  fun `when the login call fails with a server error, the server error must be shown`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    val errorMessage = "Error"
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(ServerError(errorMessage)))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).showServerError(errorMessage)
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
    verify(dataSync, never()).fireAndForgetSync()
  }

  @Test
  fun `when the login call fails unexpectedly, the PIN should be cleared`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(UnexpectedError))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).clearPin()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).showUnexpectedError()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the login call fails with a network error, the PIN should be cleared`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(NetworkError))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).clearPin()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).showNetworkError()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the login call fails with a server error, the PIN should be cleared`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(ServerError("Error")))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).clearPin()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).showServerError("Error")
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the OTP changes and meets the otp length, the login call should be made`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    setupController()

    verify(screen).showUserPhoneNumber(phoneNumber)

    uiEvents.onNext(EnterOtpTextChanges("1111"))
    verifyNoMoreInteractions(screen, loginUserWithOtp)

    uiEvents.onNext(EnterOtpTextChanges("11111"))
    verifyNoMoreInteractions(screen, loginUserWithOtp)

    clearInvocations(screen)

    uiEvents.onNext(EnterOtpTextChanges("111111"))
    verify(loginUserWithOtp).loginWithOtp(phoneNumber, pin, "111111")
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).goBack()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the login call is made, the network progress must be shown`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.never<LoginResult>())

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).showProgress()
    verify(screen, never()).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the login call succeeds, the network progress must be hidden`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).goBack()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the login call fails with a network error, the network progress must be hidden`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(NetworkError))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).showNetworkError()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the login call fails with a server error, the network progress must be hidden`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(ServerError("Test")))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).showServerError("Test")
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the login call fails with an unexpected error, the network progress must be hidden`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(UnexpectedError))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).showUnexpectedError()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when a user is verified for login in the background, the screen must be closed`() {
    val userStream = Observable.just<Optional<User>>(
        Optional.of(user),
        Optional.of(user.copy(loggedInStatus = LOGGED_IN))
    )

    setupController(userStream = userStream)

    verify(screen).goBack()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when a user is not verified for login in the background, the screen must not be closed`() {
    val userStream = Observable.just<Optional<User>>(
        Optional.of(user),
        Optional.of(user.copy(loggedInStatus = OTP_REQUESTED))
    )

    setupController(userStream = userStream)

    verify(screen, never()).goBack()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when resend sms is clicked, the request otp flow should be triggered`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.Success(userPayload))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(activateUser).activate(loggedInUserUuid, pin)
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).showSmsSentMessage()
    verify(screen).hideProgress()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call is made, the progress must be shown`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.Success(userPayload))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).showSmsSentMessage()
    verify(screen).hideProgress()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call is made, any error must be hidden`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.Success(userPayload))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).showSmsSentMessage()
    verify(screen).hideProgress()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call is successful, the progress must be hidden`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.Success(userPayload))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).showSmsSentMessage()
    verify(screen).hideProgress()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call fails with a network error, the progress must be hidden`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.NetworkError)
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).showNetworkError()
    verify(screen).hideProgress()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call fails with a server error, the progress must be hidden`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.ServerError(500))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).showUnexpectedError()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call fails with an unexpected error, the progress must be hidden`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.OtherError(RuntimeException()))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).showUnexpectedError()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call succeeds, the sms sent message must be shown`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.Success(userPayload))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).showSmsSentMessage()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call fails with a network error, the sms sent message must not be shown`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.NetworkError)
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen, never()).showSmsSentMessage()
    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).showNetworkError()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call fails with a server error, the sms sent message must not be shown`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.ServerError(400))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen, never()).showSmsSentMessage()
    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).showUnexpectedError()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call fails with an unexpected error, the sms sent message must not be shown`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.OtherError(RuntimeException()))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen, never()).showSmsSentMessage()
    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).showUnexpectedError()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call completes successfully, the error must not be shown`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.Success(userPayload))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen, never()).showNetworkError()
    verify(screen, never()).showServerError(any())
    verify(screen, never()).showUnexpectedError()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).clearPin()
    verify(screen).showSmsSentMessage()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call fails with a network error, the error must be shown`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.NetworkError)
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).showNetworkError()
    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call fails with a server error, the error must be shown`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.ServerError(400))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).showUnexpectedError()
    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call fails with an unexpected error, the error must be shown`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.OtherError(RuntimeException()))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).showUnexpectedError()
    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call fails with a network error, the PIN must be cleared`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.NetworkError)
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).clearPin()
    verify(screen).showNetworkError()
    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call fails with a server error, the PIN must be cleared`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.ServerError(400))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).clearPin()
    verify(screen).showUnexpectedError()
    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the resend sms call fails with an unexpected error, the PIN must be cleared`() {
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.OtherError(RuntimeException()))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    verify(screen).clearPin()
    verify(screen).showUnexpectedError()
    verify(screen).hideProgress()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).hideError()
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the login call succeeds, the ongoing login entry must be cleared`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(ongoingLoginEntryRepository).clearLoginEntry()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).goBack()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the login call fails, the ongoing login entry must not be cleared`() {
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(NetworkError))

    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    verify(ongoingLoginEntryRepository, never()).clearLoginEntry()
    verify(screen).showUserPhoneNumber(phoneNumber)
    verify(screen).showProgress()
    verify(screen).hideProgress()
    verify(screen).showNetworkError()
    verify(screen).clearPin()
    verifyNoMoreInteractions(screen)
  }

  private fun setupController(
      user: User = this.user,
      userStream: Observable<Optional<User>> = Observable.just(Optional.of(user))
  ) {
    whenever(userSession.loggedInUserImmediate()) doReturn user
    whenever(userSession.loggedInUser()) doReturn userStream

    val controller = EnterOtpScreenController(
        userSession = userSession,
        activateUser = activateUser,
        loginUserWithOtp = loginUserWithOtp,
        ongoingLoginEntryRepository = ongoingLoginEntryRepository,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        dataSync = dataSync
    )

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    uiEvents.onNext(ScreenCreated())
  }
}
