package org.simple.clinic.user

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.analytics.MockReporter
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.forgotpin.ForgotPinApiV1
import org.simple.clinic.forgotpin.ResetPinRequest
import org.simple.clinic.login.LoginApiV1
import org.simple.clinic.login.LoginResponse
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.applock.PasswordHasher
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.FindUserResult
import org.simple.clinic.registration.RegistrationApiV1
import org.simple.clinic.registration.RegistrationResponse
import org.simple.clinic.registration.RegistrationResult
import org.simple.clinic.registration.SaveUserLocallyResult
import org.simple.clinic.sync.SyncScheduler
import org.simple.clinic.util.Optional
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class UserSessionTest {

  private val loginApi = mock<LoginApiV1>()
  private val forgotPinApiV1 = mock<ForgotPinApiV1>()
  private val registrationApi = mock<RegistrationApiV1>()
  private val accessTokenPref = mock<Preference<Optional<String>>>()
  private val facilitySync = mock<FacilitySync>()
  private val facilityRepository = mock<FacilityRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val sharedPrefs = mock<SharedPreferences>()
  private val appDatabase = mock<AppDatabase>()
  private val passwordHasher = mock<PasswordHasher>()
  private val userDao = mock<User.RoomDao>()
  private val reporter = MockReporter()

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
  private lateinit var syncScheduler: SyncScheduler

  @Before
  fun setUp() {
    syncScheduler = mock()

    userSession = UserSession(
        loginApi,
        registrationApi,
        forgotPinApiV1,
        moshi,
        facilitySync,
        facilityRepository,
        sharedPrefs,
        appDatabase,
        passwordHasher,
        accessTokenPref,
        syncScheduler
    )
    userSession.saveOngoingLoginEntry(OngoingLoginEntry(UUID.randomUUID(), "phone", "pin")).blockingAwait()
    whenever(facilitySync.sync()).thenReturn(Completable.complete())
    whenever(patientRepository.clearPatientData()).thenReturn(Completable.complete())

    whenever(appDatabase.userDao()).thenReturn(userDao)

    whenever(facilityRepository.associateUserWithFacilities(any(), any(), any())).thenReturn(Completable.complete())

    Analytics.addReporter(reporter)
  }

  @Test
  fun `login should correctly map network response to result`() {
    whenever(loginApi.login(any()))
        .thenReturn(Single.just(LoginResponse("accessToken", loggedInUserPayload)))
        .thenReturn(Single.error(NullPointerException()))
        .thenReturn(Single.error(unauthorizedHttpError<LoginResponse>()))
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

  private fun <T> unauthorizedHttpError(): HttpException {
    val error = Response.error<T>(401, ResponseBody.create(MediaType.parse("text"), unauthorizedErrorResponseJson))
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
  fun `when the server sends a user without facilities during registration then registration should be canceled`() {
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
  fun `when ongoing registration entry is cleared then isOngoingRegistrationEntryPresent() should emit false`() {
    userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry())
        .andThen(userSession.clearOngoingRegistrationEntry())
        .andThen(userSession.isOngoingRegistrationEntryPresent())
        .test()
        .await()
        .assertValue(false)
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

  @Test
  fun `when requesting login otp fails, the local logged in status must not be updated`() {
    whenever(loginApi.requestLoginOtp(any()))
        .thenReturn(
            Completable.error(RuntimeException()),
            Completable.error(IOException())
        )

    userSession.requestLoginOtp().blockingGet()
    verify(userDao, never()).updateLoggedInStatusForUser(any(), any())

    userSession.requestLoginOtp().blockingGet()
    verify(userDao, never()).updateLoggedInStatusForUser(any(), any())
  }

  @Test
  fun `when the reset PIN flow is started, the sync must be triggered`() {
    whenever(syncScheduler.syncImmediately()).thenReturn(Completable.complete())

    val user = PatientMocker.loggedInUser()
    whenever(userDao.user()).thenReturn(Flowable.just(listOf(user)))

    userSession.startForgotPinFlow(patientRepository).blockingAwait()

    verify(syncScheduler).syncImmediately()
  }

  @Test
  @Parameters(value = ["0", "1", "2"])
  fun `if the sync fails when resetting PIN, it should be retried and complete if any retry succeeds`(retryCount: Int) {
    // Mockito doesn't have a way to specify a vararg for all invocations and expects
    // the first emission to be explicitly provided. This dynamically constructs the
    // rest of the emissions and ensures that the last one succeeds.
    val emissionsAfterFirst: Array<Completable> = (0 until retryCount)
        .map { retryIndex ->
          if (retryIndex == retryCount - 1) Completable.complete() else Completable.error(RuntimeException())
        }.toTypedArray()

    whenever(syncScheduler.syncImmediately())
        .thenReturn(Completable.error(RuntimeException()), *emissionsAfterFirst)

    val user = PatientMocker.loggedInUser()
    whenever(userDao.user()).thenReturn(Flowable.just(listOf(user)))

    userSession.startForgotPinFlow(patientRepository, retryCount)
        .test()
        .await()
        .assertComplete()
  }

  @Test
  @Parameters(value = ["0", "1", "2"])
  fun `if the sync fails when resetting PIN, it should be retried and complete if all retries fail`(retryCount: Int) {
    val emissionsAfterFirst: Array<Completable> = (0 until retryCount)
        .map { Completable.error(RuntimeException()) }.toTypedArray()

    whenever(syncScheduler.syncImmediately())
        .thenReturn(Completable.error(RuntimeException()), *emissionsAfterFirst)

    val user = PatientMocker.loggedInUser()
    whenever(userDao.user()).thenReturn(Flowable.just(listOf(user)))

    userSession.startForgotPinFlow(patientRepository, retryCount)
        .test()
        .await()
        .assertComplete()
  }

  @Test
  fun `if the sync succeeds when resetting the PIN, it should clear the patient related data`() {
    whenever(syncScheduler.syncImmediately()).thenReturn(Completable.complete())

    val user = PatientMocker.loggedInUser()
    whenever(userDao.user()).thenReturn(Flowable.just(listOf(user)))

    userSession.startForgotPinFlow(patientRepository)
        .test()
        .await()

    verify(patientRepository).clearPatientData()
  }

  @Test
  fun `if the sync fails when resetting the PIN, it should clear the patient related data`() {
    whenever(syncScheduler.syncImmediately()).thenReturn(Completable.complete())

    val user = PatientMocker.loggedInUser()
    whenever(userDao.user()).thenReturn(Flowable.just(listOf(user)))

    userSession.startForgotPinFlow(patientRepository)
        .test()
        .await()

    verify(patientRepository).clearPatientData()
  }

  @Test
  fun `after clearing patient related data during forgot PIN flow, the user status must be set to RESETTING_PIN`() {
    whenever(syncScheduler.syncImmediately()).thenReturn(Completable.complete())

    val user = PatientMocker.loggedInUser()
    whenever(userDao.user()).thenReturn(Flowable.just(listOf(user)))

    userSession.startForgotPinFlow(patientRepository)
        .test()
        .await()

    verify(userDao).updateLoggedInStatusForUser(user.uuid, User.LoggedInStatus.RESETTING_PIN)
  }

  @Test
  @Parameters(value = [
    "0000|password-1",
    "1111|password-2"
  ])
  fun `when reset PIN request is raised, the network call must be made with the hashed PIN`(
      pin: String,
      hashed: String
  ) {
    val currentUser = PatientMocker.loggedInUser(pinDigest = "old-digest", loggedInStatus = User.LoggedInStatus.RESETTING_PIN)
    whenever(userDao.user()).thenReturn(Flowable.just(listOf(currentUser)))

    whenever(passwordHasher.hash(any())).thenReturn(Single.just(hashed))
    whenever(forgotPinApiV1.resetPin(any())).thenReturn(Completable.complete())

    userSession.resetPin(pin).blockingGet()

    verify(forgotPinApiV1).resetPin(ResetPinRequest(hashed))
  }

  @Test
  fun `when the password hashing fails on resetting PIN, an expected error must be thrown`() {
    val currentUser = PatientMocker.loggedInUser(pinDigest = "old-digest", loggedInStatus = User.LoggedInStatus.RESETTING_PIN)
    whenever(userDao.user()).thenReturn(Flowable.just(listOf(currentUser)))

    val exception = RuntimeException()
    whenever(passwordHasher.hash(any())).thenReturn(Single.error(exception))

    val result = userSession.resetPin("0000").blockingGet()
    assertThat(result).isEqualTo(ForgotPinResult.UnexpectedError(exception))
  }

  @Test
  @Parameters(method = "params for forgot pin api")
  fun `the appropriate result must be returned when the reset pin call finishes`(
      apiResult: Completable,
      expectedResult: ForgotPinResult
  ) {
    val currentUser = PatientMocker.loggedInUser(pinDigest = "old-digest", loggedInStatus = User.LoggedInStatus.RESETTING_PIN)
    whenever(userDao.user()).thenReturn(Flowable.just(listOf(currentUser)))

    whenever(passwordHasher.hash(any())).thenReturn(Single.just("hashed"))
    whenever(forgotPinApiV1.resetPin(any())).thenReturn(apiResult)

    val result = userSession.resetPin("0000").blockingGet()
    assertThat(result).isEqualTo(expectedResult)
  }

  @Suppress("Unused")
  private fun `params for forgot pin api`(): Array<Array<Any>> {
    val exception = RuntimeException()
    return arrayOf(
        arrayOf(Completable.error(IOException()), ForgotPinResult.NetworkError),
        arrayOf(Completable.error(unauthorizedHttpError<Any>()), ForgotPinResult.UserNotFound),
        arrayOf(Completable.error(exception), ForgotPinResult.UnexpectedError(exception))
    )
  }

  @Test
  fun `whenever the forgot pin api succeeds, the logged in users password digest and logged in status must be updated`() {
    val currentUser = PatientMocker.loggedInUser(
        pinDigest = "old-digest",
        loggedInStatus = User.LoggedInStatus.RESETTING_PIN,
        status = UserStatus.APPROVED_FOR_SYNCING
    )
    whenever(userDao.user()).thenReturn(Flowable.just(listOf(currentUser)))

    whenever(passwordHasher.hash(any())).thenReturn(Single.just("new-digest"))
    whenever(forgotPinApiV1.resetPin(any())).thenReturn(Completable.complete())

    userSession.resetPin("0000").blockingGet()

    verify(userDao).createOrUpdate(currentUser.copy(
        pinDigest = "new-digest",
        loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED,
        status = UserStatus.WAITING_FOR_APPROVAL
    ))
  }

  @Test
  fun `whenever the forgot pin api call fails, the logged in user must not be updated`() {
    val currentUser = PatientMocker.loggedInUser(pinDigest = "old-digest", loggedInStatus = User.LoggedInStatus.RESETTING_PIN)
    whenever(userDao.user()).thenReturn(Flowable.just(listOf(currentUser)))

    whenever(passwordHasher.hash(any())).thenReturn(Single.just("hashed"))
    whenever(forgotPinApiV1.resetPin(any())).thenReturn(Completable.error(RuntimeException()))

    userSession.resetPin("0000").blockingGet()

    verify(userDao, never()).createOrUpdate(any())
  }

  @Test
  @Parameters(method = "params for saving user after reset pin")
  fun `the appropriate response must be returned when saving the user after reset PIN call succeeds`(
      errorToThrow: Throwable?,
      expectedResult: ForgotPinResult
  ) {
    val currentUser = PatientMocker.loggedInUser(pinDigest = "old-digest", loggedInStatus = User.LoggedInStatus.RESETTING_PIN)
    whenever(userDao.user()).thenReturn(Flowable.just(listOf(currentUser)))

    errorToThrow?.let { whenever(userDao.createOrUpdate(any())).doThrow(it) }

    whenever(passwordHasher.hash(any())).thenReturn(Single.just("new-digest"))
    whenever(forgotPinApiV1.resetPin(any())).thenReturn(Completable.complete())

    val result = userSession.resetPin("0000").blockingGet()

    assertThat(result).isEqualTo(expectedResult)
  }

  @Suppress("Unused")
  private fun `params for saving user after reset pin`(): Array<Array<Any?>> {
    val exception = java.lang.RuntimeException()
    return arrayOf(
        arrayOf<Any?>(null, ForgotPinResult.Success),
        arrayOf<Any?>(exception, ForgotPinResult.UnexpectedError(exception))
    )
  }

  @After
  fun tearDown() {
    reporter.clear()
    Analytics.clearReporters()
  }
}
