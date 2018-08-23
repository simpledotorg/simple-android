package org.simple.clinic.user

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.simple.clinic.AppDatabase
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.login.LoginApiV1
import org.simple.clinic.login.LoginResponse
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.applock.PasswordHasher
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.registration.FindUserResult
import org.simple.clinic.registration.RegistrationApiV1
import org.simple.clinic.registration.RegistrationResponse
import org.simple.clinic.registration.RegistrationResult
import org.simple.clinic.registration.SaveUserLocallyResult
import org.simple.clinic.util.Optional
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.util.UUID

class UserSessionTest {

  private val loginApi = mock<LoginApiV1>()
  private val registrationApi = mock<RegistrationApiV1>()
  private val accessTokenPref = mock<Preference<Optional<String>>>()
  private val facilitySync = mock<FacilitySync>()
  private val facilityRepository = mock<FacilityRepository>()
  private val sharedPrefs = mock<SharedPreferences>()
  private val appDatabase = mock<AppDatabase>()
  private val passwordHasher = mock<PasswordHasher>()
  private val userDao = mock<User.RoomDao>()

  private val moshi = Moshi.Builder().build()
  private val loggedInUserPayload = PatientMocker.loggedInUserPayload()
  private val unauthorizedErrorResponseJson = """{
        "errors": {
          "user": [
            "user is not present"
          ]
        }
      }"""

  private lateinit var userSession: UserSession

  @Before
  fun setUp() {
    userSession = UserSession(
        loginApi,
        registrationApi,
        moshi,
        facilitySync,
        facilityRepository,
        sharedPrefs,
        appDatabase,
        passwordHasher,
        accessTokenPref
    )
    userSession.saveOngoingLoginEntry(OngoingLoginEntry(UUID.randomUUID(), "phone", "pin")).blockingAwait()
    whenever(facilitySync.sync()).thenReturn(Completable.complete())

    whenever(appDatabase.userDao()).thenReturn(userDao)

    whenever(facilityRepository.associateUserWithFacilities(any(), any(), any())).thenReturn(Completable.complete())
  }

  @Test
  fun `login should correctly map network response to result`() {
    whenever(loginApi.login(any()))
        .thenReturn(Single.just(LoginResponse("accessToken", loggedInUserPayload)))
        .thenReturn(Single.error(NullPointerException()))
        .thenReturn(Single.error(unauthorizedHttpError()))
        .thenReturn(Single.error(SocketTimeoutException()))

    val result1 = userSession.loginWithOtp("").blockingGet()
    assertThat(result1).isInstanceOf(LoginResult.Success::class.java)

    val result2 = userSession.loginWithOtp("").blockingGet()
    assertThat(result2).isInstanceOf(LoginResult.UnexpectedError::class.java)

    val result3 = userSession.loginWithOtp("").blockingGet()
    assertThat(result3).isInstanceOf(LoginResult.ServerError::class.java)

    val result4 = userSession.loginWithOtp("").blockingGet()
    assertThat(result4).isInstanceOf(LoginResult.NetworkError::class.java)
  }

  private fun unauthorizedHttpError(): HttpException {
    val error = Response.error<LoginResponse>(401, ResponseBody.create(MediaType.parse("text"), unauthorizedErrorResponseJson))
    return HttpException(error)
  }

  @Test
  fun `when find existing user then the network response should correctly be mapped to results`() {
    val notFoundHttpError = mock<HttpException>()
    whenever(notFoundHttpError.code()).thenReturn(404)

    whenever(registrationApi.findUser("123")).thenReturn(Single.just(PatientMocker.loggedInUserPayload()))
    whenever(registrationApi.findUser("456")).thenReturn(Single.error(SocketTimeoutException()))
    whenever(registrationApi.findUser("789")).thenReturn(Single.error(notFoundHttpError))
    whenever(registrationApi.findUser("000")).thenReturn(Single.error(NullPointerException()))

    val result1 = userSession.findExistingUser("123").blockingGet()
    assertThat(result1).isInstanceOf(FindUserResult.Found::class.java)

    val result2 = userSession.findExistingUser("456").blockingGet()
    assertThat(result2).isInstanceOf(FindUserResult.NetworkError::class.java)

    val result3 = userSession.findExistingUser("789").blockingGet()
    assertThat(result3).isInstanceOf(FindUserResult.NotFound::class.java)

    val result4 = userSession.findExistingUser("000").blockingGet()
    assertThat(result4).isInstanceOf(FindUserResult.UnexpectedError::class.java)
  }

  @Test
  fun `when the server sends a user without facilities during registration ethen registration should be canceled`() {
    whenever(appDatabase.userDao().user()).thenReturn(Flowable.just(listOf(PatientMocker.loggedInUser())))

    val userFacility = PatientMocker.facility()
    whenever(facilityRepository.facilityUuidsForUser(any())).thenReturn(Observable.just(listOf(userFacility.uuid)))

    val response = RegistrationResponse(
        userPayload = PatientMocker.loggedInUserPayload(facilityUuids = emptyList()),
        accessToken = "token")
    whenever(registrationApi.createUser(any())).thenReturn(Single.just(response))

    val registrationResult = userSession.register().blockingGet()
    assertThat(registrationResult).isInstanceOf(RegistrationResult.Error::class.java)
  }

  @Test
  fun `when refreshing the logged in user then the user details should be fetched from the server`() {
    val loggedInUser = PatientMocker.loggedInUser(uuid = loggedInUserPayload.uuid)
    val refreshedUserPayload = loggedInUserPayload.copy(status = UserStatus.APPROVED_FOR_SYNCING)

    whenever(registrationApi.findUser(loggedInUserPayload.phoneNumber)).thenReturn(Single.just(refreshedUserPayload))
    whenever(loginApi.login(any())).thenReturn(Single.just(LoginResponse("accessToken", loggedInUserPayload)))
    whenever(facilityRepository.associateUserWithFacilities(any(), any(), any())).thenReturn(Completable.complete())
    whenever(userDao.user()).thenReturn(Flowable.just(listOf(loggedInUser)))

    userSession.refreshLoggedInUser().blockingAwait()

    verify(registrationApi).findUser(loggedInUserPayload.phoneNumber)
    verify(userDao).createOrUpdate(check {
      assertThat(it.uuid).isEqualTo(loggedInUserPayload.uuid)
      assertThat(it.status).isEqualTo(UserStatus.APPROVED_FOR_SYNCING)
    })
  }

  @Test
  fun `when saving the user locally and the facility sync fails, the network error should correctly map results`() {
    whenever(facilitySync.pullWithResult())
        .thenReturn(
            Single.just(FacilityPullResult.Success()),
            Single.just(FacilityPullResult.UnexpectedError()),
            Single.just(FacilityPullResult.NetworkError())
        )

    var result = userSession.syncFacilityAndSaveUser(loggedInUserPayload).blockingGet()
    assertThat(result).isInstanceOf(SaveUserLocallyResult.Success::class.java)

    result = userSession.syncFacilityAndSaveUser(loggedInUserPayload).blockingGet()
    assertThat(result).isInstanceOf(SaveUserLocallyResult.UnexpectedError::class.java)

    result = userSession.syncFacilityAndSaveUser(loggedInUserPayload).blockingGet()
    assertThat(result).isInstanceOf(SaveUserLocallyResult.NetworkError::class.java)
  }
}
