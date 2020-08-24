package org.simple.clinic.login

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.analytics.MockAnalyticsReporter
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.login.LoginResult.NetworkError
import org.simple.clinic.login.LoginResult.ServerError
import org.simple.clinic.login.LoginResult.Success
import org.simple.clinic.login.LoginResult.UnexpectedError
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.platform.analytics.AnalyticsUser
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.toUser
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.UUID

class LoginUserWithOtpTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val usersApi: UsersApi = mock()
  private val userDao: User.RoomDao = mock()
  private val facilityRepository = mock<FacilityRepository>()
  private val moshi = Moshi.Builder().build()
  private val accessTokenPref = mock<Preference<Optional<String>>>()
  private val analyticsReporter: MockAnalyticsReporter = MockAnalyticsReporter()

  private val phoneNumber = "1234567890"
  private val pin = "1234"
  private val otp = "000000"
  private val loginRequest = LoginRequest(UserPayload(phoneNumber, pin, otp))

  private val userUuid = UUID.fromString("34bf96ef-06f3-4d1c-9ef1-9a55cb1904e9")
  private val facilityUuid = UUID.fromString("18bd9def-ab06-491f-97bf-8fbd74f213cd")
  private val accessToken = "token"
  private val loggedInUserPayload = TestData.loggedInUserPayload(uuid = userUuid, registrationFacilityUuid = facilityUuid)
  private val loginResponse = LoginResponse(accessToken, loggedInUserPayload)

  private val loginUserWithOtp = LoginUserWithOtp(
      usersApi = usersApi,
      userDao = userDao,
      facilityRepository = facilityRepository,
      moshi = moshi,
      accessTokenPreference = accessTokenPref
  )

  @Before
  fun setUp() {
    Analytics.addReporter(analyticsReporter)
  }

  @After
  fun tearDown() {
    Analytics.clearReporters()
  }

  @Test
  fun `when the login call is successful, the access token must be saved`() {
    // given
    whenever(usersApi.login(loginRequest)) doReturn Single.just(loginResponse)
    whenever(facilityRepository.setCurrentFacility(facilityUuid)) doReturn Completable.complete()

    // when
    val loginResult = loginUserWithOtp.loginWithOtp(phoneNumber = phoneNumber, pin = pin, otp = otp).blockingGet()

    // then
    verify(accessTokenPref).set(Just(accessToken))
    assertThat(loginResult).isEqualTo(Success)
  }

  @Test
  fun `when the login call is successful, the user must be saved`() {
    // given
    whenever(usersApi.login(loginRequest)) doReturn Single.just(loginResponse)
    val user = loggedInUserPayload.toUser(LOGGED_IN)
    whenever(facilityRepository.setCurrentFacility(facilityUuid)) doReturn Completable.complete()

    // when
    val loginResult = loginUserWithOtp.loginWithOtp(phoneNumber = phoneNumber, pin = pin, otp = otp).blockingGet()

    // then
    verify(userDao).createOrUpdate(user)
    assertThat(loginResult).isEqualTo(Success)
  }

  @Test
  fun `when the login call is successful, the user must be reported to analytics`() {
    // given
    whenever(usersApi.login(loginRequest)) doReturn Single.just(loginResponse)
    val user = loggedInUserPayload.toUser(LOGGED_IN)
    whenever(facilityRepository.setCurrentFacility(facilityUuid)) doReturn Completable.complete()

    // when
    val loginResult = loginUserWithOtp.loginWithOtp(phoneNumber = phoneNumber, pin = pin, otp = otp).blockingGet()

    // then
    assertThat(analyticsReporter.user).isEqualTo(AnalyticsUser(user.uuid, user.fullName))
    assertThat(loginResult).isEqualTo(Success)
  }

  @Test
  fun `when the login call fails with a network error, the network error result must be returned`() {
    // given
    whenever(usersApi.login(loginRequest)) doReturn Single.error<LoginResponse>(IOException())

    // when
    val loginResult = loginUserWithOtp.loginWithOtp(phoneNumber = phoneNumber, pin = pin, otp = otp).blockingGet()

    // then
    verifyZeroInteractions(accessTokenPref)
    verifyZeroInteractions(userDao)
    verifyZeroInteractions(facilityRepository)
    assertThat(analyticsReporter.user).isNull()
    assertThat(loginResult).isEqualTo(NetworkError)
  }

  @Test
  fun `when the login call fails with an unauthenticated error, the server error result must be returned`() {
    // given
    // TODO(vs): 2019-11-15 This is tied to the implementation detail, extract to another class
    val errorReason = "user is not present"
    val errorJson = """{
        "errors": {
          "user": [
            "$errorReason"
          ]
        }
      }"""
    val error = HttpException(Response.error<LoginResponse>(401, ResponseBody.create(MediaType.parse("text"), errorJson)))
    whenever(usersApi.login(loginRequest)) doReturn Single.error<LoginResponse>(error)

    // when
    val loginResult = loginUserWithOtp.loginWithOtp(phoneNumber = phoneNumber, pin = pin, otp = otp).blockingGet()

    // then
    verifyZeroInteractions(accessTokenPref)
    verifyZeroInteractions(userDao)
    verifyZeroInteractions(facilityRepository)
    assertThat(analyticsReporter.user).isNull()
    assertThat(loginResult).isEqualTo(ServerError(errorReason))
  }

  @Test
  fun `when the login call fails with any other error, a generic error response should be returned`() {
    // given
    whenever(usersApi.login(loginRequest)) doReturn Single.error<LoginResponse>(RuntimeException())

    // when
    val loginResult = loginUserWithOtp.loginWithOtp(phoneNumber = phoneNumber, pin = pin, otp = otp).blockingGet()

    // then
    verifyZeroInteractions(accessTokenPref)
    verifyZeroInteractions(userDao)
    verifyZeroInteractions(facilityRepository)
    assertThat(analyticsReporter.user).isNull()
    assertThat(loginResult).isEqualTo(UnexpectedError)
  }
}
