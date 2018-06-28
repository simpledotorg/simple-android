package org.simple.clinic.user

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.squareup.moshi.Moshi
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.simple.clinic.login.LoginApiV1
import org.simple.clinic.login.LoginResponse
import org.simple.clinic.login.LoginResult
import org.simple.clinic.util.Optional
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException

class UserSessionTest {

  private val api = mock<LoginApiV1>()
  private val loggedInUserPref = mock<Preference<Optional<LoggedInUser>>>()
  private val accessTokenPref = mock<Preference<Optional<String>>>()
  private val moshi = Moshi.Builder().build()

  private lateinit var userSession: UserSession

  companion object {
    const val UNAUTHORIZED_ERROR_RESPONSE_JSON = """{
        "errors": {
          "user": [
            "user is not present"
          ]
        }
      }"""
  }

  @Before
  fun setUp() {
    userSession = UserSession(api, loggedInUserPref, moshi, accessTokenPref)
  }

  @Test
  fun `login should correctly map network response to result`() {
    userSession.saveOngoingLoginEntry(OngoingLoginEntry("otp", "phone", "pin")).blockingAwait()

    val loggedInUser = LoggedInUser(
        uuid = mock(),
        fullName = "a name",
        phoneNumber = "a phone",
        passwordDigest = "a hash",
        facilityUuid = mock(),
        createdAt = mock(),
        updatedAt = mock())

    val error = Response.error<LoginResponse>(401, ResponseBody.create(MediaType.parse("text"), UNAUTHORIZED_ERROR_RESPONSE_JSON))
    val unauthorizedHttpError = HttpException(error)

    whenever(api.login(any()))
        .thenReturn(Single.just(LoginResponse("accessToken", loggedInUser)))
        .thenReturn(Single.error(NullPointerException()))
        .thenReturn(Single.error(unauthorizedHttpError))
        .thenReturn(Single.error(SocketTimeoutException()))

    val result1 = userSession.login().blockingGet()
    assertThat(result1).isInstanceOf(LoginResult.Success::class.java)

    val result2 = userSession.login().blockingGet()
    assertThat(result2).isInstanceOf(LoginResult.UnexpectedError::class.java)

    val result3 = userSession.login().blockingGet()
    assertThat(result3).isInstanceOf(LoginResult.ServerError::class.java)

    val result4 = userSession.login().blockingGet()
    assertThat(result4).isInstanceOf(LoginResult.NetworkError::class.java)
  }
}
