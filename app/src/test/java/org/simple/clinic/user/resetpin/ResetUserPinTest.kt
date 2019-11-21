package org.simple.clinic.user.resetpin

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
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
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.UserStatus.WaitingForApproval
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.toPayload
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class ResetUserPinTest {

  private val passwordHasher = JavaHashPasswordHasher()
  private val loginApi = mock<LoginApi>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userDao = mock<User.RoomDao>()
  private val accessTokenPref = mock<Preference<Optional<String>>>()

  private val currentUser = PatientMocker.loggedInUser(
      uuid = UUID.fromString("36f6072c-0757-43e6-9a09-2bb9971cc7d3"),
      pinDigest = hash("0000"),
      loggedInStatus = RESETTING_PIN
  )
  private val facilityUuid = UUID.fromString("4ffa1d2b-f023-4239-91ad-7fb7ddfddaab")
  private val newPin = "1234"
  private val newPinDigest = hash(newPin)
  private val updatedUser = currentUser.afterPinResetRequested(newPinDigest)

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
    whenever(facilityRepository.associateUserWithFacilities(updatedUser, listOf(facilityUuid), facilityUuid)) doReturn Completable.complete()
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))

    errorToThrow?.let { whenever(userDao.createOrUpdate(updatedUser)) doThrow it }

    val response = ForgotPinResponse(
        loggedInUser = updatedUser.toPayload(facilityUuid),
        accessToken = "new_access_token"
    )
    whenever(loginApi.resetPin(ResetPinRequest(newPinDigest))) doReturn Single.just(response)

    val result = resetUserPin.resetPin(newPin).blockingGet()

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
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))

    val newPinDigest = hash(newPin)
    whenever(loginApi.resetPin(ResetPinRequest(newPinDigest))) doReturn apiResult

    val result = resetUserPin.resetPin(newPin).blockingGet()
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
    "0000",
    "1111"
  ])
  fun `when reset PIN request is raised, the network call must be made with the hashed PIN`(pin: String) {
    val hashed = hash(pin)
    val updatedUser = currentUser.afterPinResetRequested(hashed)

    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))
    whenever(facilityRepository.associateUserWithFacilities(updatedUser, listOf(facilityUuid), facilityUuid)) doReturn Completable.complete()

    whenever(loginApi.resetPin(ResetPinRequest(hashed))) doReturn Single.just(ForgotPinResponse(
        accessToken = "",
        loggedInUser = updatedUser.toPayload(facilityUuid)
    ))

    resetUserPin.resetPin(pin).blockingGet()

    verify(loginApi).resetPin(ResetPinRequest(hashed))
  }

  @Test
  fun `whenever the forgot pin api call fails, the access token must not be updated`() {
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))

    whenever(loginApi.resetPin(ResetPinRequest(newPinDigest))) doReturn Single.error<ForgotPinResponse>(RuntimeException())

    resetUserPin.resetPin(newPin).blockingGet()

    verify(accessTokenPref, never()).set(any())
  }

  @Test
  fun `whenever the forgot pin api call fails, the logged in user must not be updated`() {
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))
    whenever(loginApi.resetPin(ResetPinRequest(newPinDigest))) doReturn Single.error<ForgotPinResponse>(RuntimeException())

    resetUserPin.resetPin(newPin).blockingGet()

    verify(userDao, never()).createOrUpdate(any())
  }

  @Test
  fun `whenever the forgot pin api succeeds, the access token must be updated`() {
    val updatedAccessToken = "new_access_token"

    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))
    whenever(facilityRepository.associateUserWithFacilities(updatedUser, listOf(facilityUuid), facilityUuid)) doReturn Completable.complete()

    val response = ForgotPinResponse(
        loggedInUser = updatedUser.toPayload(facilityUuid),
        accessToken = updatedAccessToken
    )
    whenever(loginApi.resetPin(ResetPinRequest(newPinDigest))) doReturn Single.just(response)

    resetUserPin.resetPin(newPin).blockingGet()

    verify(accessTokenPref).set(Just(updatedAccessToken))
  }

  @Test
  fun `whenever the forgot pin api succeeds, the logged in users password digest and logged in status must be updated`() {
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))
    whenever(facilityRepository.associateUserWithFacilities(updatedUser, listOf(facilityUuid), facilityUuid)) doReturn Completable.complete()

    val response = ForgotPinResponse(
        loggedInUser = updatedUser.toPayload(facilityUuid),
        accessToken = "new_access_token"
    )
    whenever(loginApi.resetPin(ResetPinRequest(newPinDigest))) doReturn Single.just(response)

    resetUserPin.resetPin(newPin).blockingGet()

    verify(userDao).createOrUpdate(updatedUser)
  }

  private fun hash(pin: String): String = passwordHasher.hash(pin).blockingGet()

  private fun <T> unauthorizedHttpError(): HttpException {
    val error = Response.error<T>(401, ResponseBody.create(MediaType.parse("text"), unauthorizedErrorResponseJson))
    return HttpException(error)
  }

  private fun User.afterPinResetRequested(updatedPinDigest: String): User {
    return copy(pinDigest = updatedPinDigest, status = WaitingForApproval, loggedInStatus = RESET_PIN_REQUESTED)
  }
}
