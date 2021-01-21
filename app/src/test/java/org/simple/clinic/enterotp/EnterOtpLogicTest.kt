package org.simple.clinic.enterotp

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
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
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class EnterOtpLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val userSession = mock<UserSession>()
  private val ui = mock<EnterOtpUi>()
  private val uiActions = mock<EnterOtpUiActions>()
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
      updatedAt = user.updatedAt,
      capabilities = user.capabilities
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

  private lateinit var testFixture: MobiusTestFixture<EnterOtpModel, EnterOtpEvent, EnterOtpEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when the screen is created, the logged in users phone number must be shown`() {
    // when
    setupController()

    // then
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when an otp of the right length is submitted, an error must not be shown`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, never()).showIncorrectOtpError()
    verify(ui, times(2)).hideProgress()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when an otp lesser than the required length is submitted, an error must be shown`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted("11111"))

    // then
    verifyZeroInteractions(loginUserWithOtp)
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showIncorrectOtpError()
    verify(uiActions).clearPin()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when an otp of the required length is submitted, the login call must be made`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(loginUserWithOtp).loginWithOtp(phoneNumber, pin, otp)
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, times(2)).hideProgress()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when an otp lesser than the required length is submitted, the login call must not be made`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted("11111"))

    // then
    verifyZeroInteractions(loginUserWithOtp)
    verify(loginUserWithOtp, never()).loginWithOtp(phoneNumber, pin, otp)
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showIncorrectOtpError()
    verify(uiActions).clearPin()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the otp is submitted, the login call must be made`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(loginUserWithOtp).loginWithOtp(phoneNumber, pin, otp)
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, times(2)).hideProgress()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the login call succeeds, the screen must be closed`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, times(2)).hideProgress()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the login call succeeds, a complete sync must be triggered`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(dataSync).fireAndForgetSync()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, times(2)).hideProgress()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the login call fails unexpectedly, the generic error must be shown`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(UnexpectedError))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(ui).showUnexpectedError()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui).hideProgress()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
    verify(dataSync, never()).fireAndForgetSync()
  }

  @Test
  fun `when the login call fails with a network error, the network error must be shown`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(NetworkError))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(ui).showNetworkError()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui).hideProgress()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
    verify(dataSync, never()).fireAndForgetSync()
  }

  @Test
  fun `when the login call fails with a server error, the server error must be shown`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    val errorMessage = "Error"
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(ServerError(errorMessage)))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(ui).showServerError(errorMessage)
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui).hideProgress()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
    verify(dataSync, never()).fireAndForgetSync()
  }

  @Test
  fun `when the login call fails unexpectedly, the PIN should be cleared`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(UnexpectedError))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(uiActions).clearPin()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui).hideProgress()
    verify(ui).showUnexpectedError()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the login call fails with a network error, the PIN should be cleared`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(NetworkError))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(uiActions).clearPin()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui).hideProgress()
    verify(ui).showNetworkError()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the login call fails with a server error, the PIN should be cleared`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(ServerError("Error")))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(uiActions).clearPin()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui).hideProgress()
    verify(ui).showServerError("Error")
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the login call is made, the network progress must be shown`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.never<LoginResult>())

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(ui).showProgress()
    verify(ui).hideProgress()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the login call succeeds, the network progress must be hidden`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(ui, times(2)).hideProgress()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the login call fails with a network error, the network progress must be hidden`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(NetworkError))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(ui).hideProgress()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui).showNetworkError()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the login call fails with a server error, the network progress must be hidden`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(ServerError("Test")))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(ui).hideProgress()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui).showServerError("Test")
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the login call fails with an unexpected error, the network progress must be hidden`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(UnexpectedError))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(ui).hideProgress()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui).showUnexpectedError()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when a user is verified for login in the background, the screen must be closed`() {
    // given
    val userStream = Observable.just<Optional<User>>(
        Optional.of(user),
        Optional.of(user.copy(loggedInStatus = LOGGED_IN))
    )

    // when
    setupController(userStream = userStream)

    // then
    verify(uiActions).goBack()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when a user is not verified for login in the background, the screen must not be closed`() {
    // given
    val userStream = Observable.just<Optional<User>>(
        Optional.of(user),
        Optional.of(user.copy(loggedInStatus = OTP_REQUESTED))
    )

    // when
    setupController(userStream = userStream)

    // then
    verify(uiActions, never()).goBack()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when resend sms is clicked, the request otp flow should be triggered`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.Success(userPayload))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(activateUser).activate(loggedInUserUuid, pin)
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(uiActions).showSmsSentMessage()
    verify(ui, times(2)).hideProgress()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call is made, the progress must be shown`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.Success(userPayload))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(uiActions).showSmsSentMessage()
    verify(ui, times(2)).hideProgress()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call is made, any error must be hidden`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.Success(userPayload))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(uiActions).showSmsSentMessage()
    verify(ui, times(2)).hideProgress()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call is successful, the progress must be hidden`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.Success(userPayload))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(uiActions).showSmsSentMessage()
    verify(ui, times(2)).hideProgress()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call fails with a network error, the progress must be hidden`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.NetworkError)
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui).showNetworkError()
    verify(ui, times(2)).hideProgress()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call fails with a server error, the progress must be hidden`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.ServerError(500))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(ui, times(2)).hideProgress()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui).showUnexpectedError()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call fails with an unexpected error, the progress must be hidden`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.OtherError(RuntimeException()))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, times(2)).hideProgress()
    verify(ui).showUnexpectedError()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call succeeds, the sms sent message must be shown`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.Success(userPayload))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(uiActions).showSmsSentMessage()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, times(2)).hideProgress()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call fails with a network error, the sms sent message must not be shown`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.NetworkError)
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(uiActions, never()).showSmsSentMessage()
    verify(ui, times(2)).hideProgress()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, times(2)).hideProgress()
    verify(ui).showNetworkError()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call fails with a server error, the sms sent message must not be shown`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.ServerError(400))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(uiActions, never()).showSmsSentMessage()
    verify(ui, times(2)).hideProgress()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui).showUnexpectedError()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call fails with an unexpected error, the sms sent message must not be shown`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.OtherError(RuntimeException()))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(uiActions, never()).showSmsSentMessage()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, times(2)).hideProgress()
    verify(ui).showUnexpectedError()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call completes successfully, the error must not be shown`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.Success(userPayload))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(ui, never()).showNetworkError()
    verify(ui, never()).showServerError(any())
    verify(ui, never()).showUnexpectedError()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, times(2)).hideProgress()
    verify(uiActions).clearPin()
    verify(uiActions).showSmsSentMessage()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call fails with a network error, the error must be shown`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.NetworkError)
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(ui).showNetworkError()
    verify(ui, times(2)).hideProgress()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, times(2)).hideProgress()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call fails with a server error, the error must be shown`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.ServerError(400))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(ui).showUnexpectedError()
    verify(ui, times(2)).hideProgress()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, times(2)).hideProgress()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call fails with an unexpected error, the error must be shown`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.OtherError(RuntimeException()))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(ui).showUnexpectedError()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, times(2)).hideProgress()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call fails with a network error, the PIN must be cleared`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.NetworkError)
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(uiActions).clearPin()
    verify(ui).showNetworkError()
    verify(ui, times(2)).hideProgress()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, times(2)).hideProgress()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call fails with a server error, the PIN must be cleared`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.ServerError(400))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(uiActions).clearPin()
    verify(ui).showUnexpectedError()
    verify(ui, times(2)).hideProgress()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the resend sms call fails with an unexpected error, the PIN must be cleared`() {
    // given
    whenever(activateUser.activate(loggedInUserUuid, pin))
        .doReturn(ActivateUser.Result.OtherError(RuntimeException()))
    whenever(ongoingLoginEntryRepository.entryImmediate())
        .doReturn(ongoingLoginEntry)

    // when
    setupController()
    uiEvents.onNext(EnterOtpResendSmsClicked())

    // then
    verify(uiActions).clearPin()
    verify(ui).showUnexpectedError()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, times(2)).hideProgress()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the login call succeeds, the ongoing login entry must be cleared`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(Success))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(ongoingLoginEntryRepository).clearLoginEntry()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui, times(2)).hideProgress()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the login call fails, the ongoing login entry must not be cleared`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()).doReturn(ongoingLoginEntry)
    whenever(loginUserWithOtp.loginWithOtp(phoneNumber, pin, otp)).doReturn(Single.just<LoginResult>(NetworkError))

    // when
    setupController()
    uiEvents.onNext(EnterOtpSubmitted(otp))

    // then
    verify(ongoingLoginEntryRepository, never()).clearLoginEntry()
    verify(ui).showUserPhoneNumber(phoneNumber)
    verify(ui).showProgress()
    verify(ui).hideProgress()
    verify(ui).showNetworkError()
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(ui, uiActions)
  }

  private fun setupController(
      user: User = this.user,
      userStream: Observable<Optional<User>> = Observable.just(Optional.of(user))
  ) {
    whenever(userSession.loggedInUserImmediate()) doReturn user
    whenever(userSession.loggedInUser()) doReturn userStream

    val effectHandler = EnterOtpEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        userSession = userSession,
        dataSync = dataSync,
        ongoingLoginEntryRepository = ongoingLoginEntryRepository,
        loginUserWithOtp = loginUserWithOtp,
        activateUser = activateUser,
        uiActions = uiActions
    )
    val uiRenderer = EnterOtpUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = EnterOtpModel.create(),
        update = EnterOtpUpdate(loginOtpRequiredLength = 6),
        effectHandler = effectHandler.build(),
        init = EnterOtpInit(),
        modelUpdateListener = uiRenderer::render
    )
    testFixture.start()
  }
}
