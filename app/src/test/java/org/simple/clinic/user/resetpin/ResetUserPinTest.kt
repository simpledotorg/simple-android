package org.simple.clinic.user.resetpin

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth
import com.google.common.truth.Truth.*
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.forgotpin.ForgotPinResponse
import org.simple.clinic.forgotpin.ResetPinRequest
import org.simple.clinic.login.LoginApi
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@RunWith(JUnitParamsRunner::class)
class ResetUserPinTest {

  private val passwordHasher = mock<PasswordHasher>()
  private val loginApi = mock<LoginApi>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userDao = mock<User.RoomDao>()
  private val accessTokenPref = mock<Preference<Optional<String>>>()

  private val unauthorizedErrorResponseJson = """{
        "errors": {
          "user": [
            "user is not present"
          ]
        }
      }"""

  private val resetUserPin = ResetUserPin(passwordHasher, loginApi, userDao, facilityRepository, accessTokenPref)

  @Test
  @Parameters(method = "params for saving user after reset pin")
  fun `the appropriate response must be returned when saving the user after reset PIN call succeeds`(
      errorToThrow: Throwable?,
      expectedResult: ResetPinResult
  ) {
    val currentUser = PatientMocker.loggedInUser(pinDigest = "old-digest", loggedInStatus = User.LoggedInStatus.RESETTING_PIN)
    whenever(facilityRepository.associateUserWithFacilities(any(), any(), any())) doReturn Completable.complete()
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))

    errorToThrow?.let { whenever(userDao.createOrUpdate(any())) doThrow it }

    whenever(passwordHasher.hash(any())) doReturn Single.just("new-digest")

    val response = ForgotPinResponse(
        loggedInUser = PatientMocker.loggedInUserPayload(
            uuid = currentUser.uuid,
            name = currentUser.fullName,
            phone = currentUser.phoneNumber,
            pinDigest = "new-digest",
            status = UserStatus.WaitingForApproval,
            createdAt = currentUser.createdAt,
            updatedAt = currentUser.updatedAt
        ),
        accessToken = "new_access_token"
    )
    whenever(loginApi.resetPin(any())) doReturn Single.just(response)

    val result = resetUserPin.resetPin("0000").blockingGet()

    assertThat(result).isEqualTo(expectedResult)
  }

  @Suppress("Unused")
  private fun `params for saving user after reset pin`(): Array<Array<Any?>> {
    val exception = java.lang.RuntimeException()
    return arrayOf(
        arrayOf<Any?>(null, ResetPinResult.Success),
        arrayOf<Any?>(exception, ResetPinResult.UnexpectedError(exception))
    )
  }

  @Test
  @Parameters(method = "params for forgot pin api")
  fun `the appropriate result must be returned when the reset pin call finishes`(
      apiResult: Single<ForgotPinResponse>,
      expectedResult: ResetPinResult
  ) {
    val currentUser = PatientMocker.loggedInUser(pinDigest = "old-digest", loggedInStatus = User.LoggedInStatus.RESETTING_PIN)
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))

    whenever(passwordHasher.hash(any())) doReturn Single.just("hashed")
    whenever(loginApi.resetPin(any())) doReturn apiResult

    val result = resetUserPin.resetPin("0000").blockingGet()
    assertThat(result).isEqualTo(expectedResult)
  }

  @Suppress("Unused")
  private fun `params for forgot pin api`(): Array<Array<Any>> {
    val exception = RuntimeException()
    return arrayOf(
        arrayOf(Single.error<ForgotPinResponse>(IOException()), ResetPinResult.NetworkError),
        arrayOf(Single.error<ForgotPinResponse>(unauthorizedHttpError<Any>()), ResetPinResult.UserNotFound),
        arrayOf(Single.error<ForgotPinResponse>(exception), ResetPinResult.UnexpectedError(exception))
    )
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
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))
    whenever(facilityRepository.associateUserWithFacilities(any(), any(), any())) doReturn Completable.complete()
    whenever(passwordHasher.hash(any())) doReturn Single.just(hashed)
    whenever(loginApi.resetPin(any())) doReturn Single.just(ForgotPinResponse(
        accessToken = "",
        loggedInUser = PatientMocker.loggedInUserPayload()
    ))

    resetUserPin.resetPin(pin).blockingGet()

    verify(loginApi).resetPin(ResetPinRequest(hashed))
  }

  @Test
  fun `when the password hashing fails on resetting PIN, an expected error must be thrown`() {
    val currentUser = PatientMocker.loggedInUser(pinDigest = "old-digest", loggedInStatus = User.LoggedInStatus.RESETTING_PIN)
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))

    val exception = RuntimeException()
    whenever(passwordHasher.hash(any())) doReturn Single.error(exception)

    val result = resetUserPin.resetPin("0000").blockingGet()
    assertThat(result).isEqualTo(ResetPinResult.UnexpectedError(exception))
  }

  @Test
  fun `whenever the forgot pin api call fails, the access token must not be updated`() {
    val currentUser = PatientMocker.loggedInUser(pinDigest = "old-digest", loggedInStatus = User.LoggedInStatus.RESETTING_PIN)
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))

    whenever(passwordHasher.hash(any())) doReturn Single.just("hashed")
    whenever(loginApi.resetPin(any())) doReturn Single.error<ForgotPinResponse>(RuntimeException())

    resetUserPin.resetPin("0000").blockingGet()

    verify(accessTokenPref, never()).set(any())
  }

  @Test
  fun `whenever the forgot pin api call fails, the logged in user must not be updated`() {
    val currentUser = PatientMocker.loggedInUser(pinDigest = "old-digest", loggedInStatus = User.LoggedInStatus.RESETTING_PIN)
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))

    whenever(passwordHasher.hash(any())) doReturn Single.just("hashed")
    whenever(loginApi.resetPin(any())) doReturn Single.error<ForgotPinResponse>(RuntimeException())

    resetUserPin.resetPin("0000").blockingGet()

    verify(userDao, never()).createOrUpdate(any())
  }

  @Test
  fun `whenever the forgot pin api succeeds, the access token must be updated`() {
    val currentUser = PatientMocker.loggedInUser(
        pinDigest = "old-digest",
        loggedInStatus = User.LoggedInStatus.RESETTING_PIN,
        status = UserStatus.ApprovedForSyncing
    )
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))
    whenever(facilityRepository.associateUserWithFacilities(any(), any(), any())) doReturn Completable.complete()
    whenever(passwordHasher.hash(any())) doReturn Single.just("new-digest")

    val response = ForgotPinResponse(
        loggedInUser = PatientMocker.loggedInUserPayload(
            uuid = currentUser.uuid,
            name = currentUser.fullName,
            phone = currentUser.phoneNumber,
            pinDigest = "new-digest",
            status = UserStatus.WaitingForApproval,
            createdAt = currentUser.createdAt,
            updatedAt = currentUser.updatedAt
        ),
        accessToken = "new_access_token"
    )
    whenever(loginApi.resetPin(any())) doReturn Single.just(response)

    resetUserPin.resetPin("0000").blockingGet()

    verify(accessTokenPref).set(Just("new_access_token"))
  }

  @Test
  fun `whenever the forgot pin api succeeds, the logged in users password digest and logged in status must be updated`() {
    val currentUser = PatientMocker.loggedInUser(
        pinDigest = "old-digest",
        loggedInStatus = User.LoggedInStatus.RESETTING_PIN,
        status = UserStatus.ApprovedForSyncing
    )
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))
    whenever(facilityRepository.associateUserWithFacilities(any(), any(), any())) doReturn Completable.complete()
    whenever(passwordHasher.hash(any())) doReturn Single.just("new-digest")

    val response = ForgotPinResponse(
        loggedInUser = PatientMocker.loggedInUserPayload(
            uuid = currentUser.uuid,
            name = currentUser.fullName,
            phone = currentUser.phoneNumber,
            pinDigest = "new-digest",
            status = UserStatus.WaitingForApproval,
            createdAt = currentUser.createdAt,
            updatedAt = currentUser.updatedAt
        ),
        accessToken = "new_access_token"
    )
    whenever(loginApi.resetPin(any())) doReturn Single.just(response)

    resetUserPin.resetPin("0000").blockingGet()

    verify(userDao).createOrUpdate(currentUser.copy(
        pinDigest = "new-digest",
        loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED,
        status = UserStatus.WaitingForApproval
    ))
  }

  private fun <T> unauthorizedHttpError(): HttpException {
    val error = Response.error<T>(401, ResponseBody.create(MediaType.parse("text"), unauthorizedErrorResponseJson))
    return HttpException(error)
  }
}
