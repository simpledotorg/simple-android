package org.simple.clinic.user

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.simple.clinic.login.LoginApiV1
import org.simple.clinic.login.LoginResponse
import org.simple.clinic.login.LoginResult
import org.simple.clinic.util.Optional
import retrofit2.HttpException
import java.net.SocketTimeoutException

class UserSessionTest {

  private val api = mock<LoginApiV1>()
  private val loggedInUserPref = mock<Preference<Optional<LoggedInUser>>>()
  private val accessTokenPref = mock<Preference<Optional<String>>>()

  private lateinit var userSession: UserSession

  @Before
  fun setUp() {
    userSession = UserSession(api, loggedInUserPref, accessTokenPref)
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

    whenever(api.login(any()))
        .thenReturn(Single.just(LoginResponse("accessToken", loggedInUser, errors = null)))
        .thenReturn(Single.error(NullPointerException()))
        .thenReturn(Single.error(mock<HttpException>()))
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
