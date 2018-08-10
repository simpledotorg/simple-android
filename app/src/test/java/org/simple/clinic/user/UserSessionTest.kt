package org.simple.clinic.user

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.whenever
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.simple.clinic.AppDatabase
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.login.LoginApiV1
import org.simple.clinic.login.LoginResponse
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.applock.PasswordHasher
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.registration.RegistrationApiV1
import org.simple.clinic.util.Optional
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException

class UserSessionTest {

  private val loginApi = mock<LoginApiV1>()
  private val registrationApi = mock<RegistrationApiV1>()
  private val accessTokenPref = mock<Preference<Optional<String>>>()
  private val facilitySync = mock<FacilitySync>()
  private val sharedPrefs = mock<SharedPreferences>()
  private val appDatabase = mock<AppDatabase>()
  private val passwordHasher = mock<PasswordHasher>()

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

    val LOGGED_IN_USER = PatientMocker.loggedInUserPayload()
  }

  @Before
  fun setUp() {
    userSession = UserSession(
        loginApi,
        registrationApi,
        moshi,
        facilitySync,
        sharedPrefs,
        appDatabase,
        passwordHasher,
        accessTokenPref
    )
    userSession.saveOngoingLoginEntry(OngoingLoginEntry("otp", "phone", "pin")).blockingAwait()
    whenever(facilitySync.sync()).thenReturn(Completable.complete())

    val mockUserDao = mock<LoggedInUser.RoomDao>()
    whenever(appDatabase.userDao()).thenReturn(mockUserDao)
  }

  @Test
  fun `login should correctly map network response to result`() {
    val unauthorizedHttpError = unauthorizedHttpError()

    whenever(loginApi.login(any()))
        .thenReturn(Single.just(LoginResponse("accessToken", LOGGED_IN_USER)))
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

  private fun unauthorizedHttpError(): HttpException {
    val error = Response.error<LoginResponse>(401, ResponseBody.create(MediaType.parse("text"), UNAUTHORIZED_ERROR_RESPONSE_JSON))
    return HttpException(error)
  }

  // TODO: Could be an Android test.
  @Test
  fun `facilities should only be synced when login succeeds`() {
    whenever(loginApi.login(any()))
        .thenReturn(Single.just(LoginResponse("accessToken", LOGGED_IN_USER)))
        .thenReturn(Single.error(NullPointerException()))
        .thenReturn(Single.error(unauthorizedHttpError()))
        .thenReturn(Single.error(SocketTimeoutException()))

    userSession.login().blockingGet()

    val inOrder = inOrder(appDatabase.userDao(), accessTokenPref, facilitySync)
    inOrder.verify(accessTokenPref).set(any())
    inOrder.verify(appDatabase.userDao()).create(any())
    inOrder.verify(facilitySync, times(1)).sync()
  }
}
