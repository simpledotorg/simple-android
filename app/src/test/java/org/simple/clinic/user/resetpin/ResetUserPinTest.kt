package org.simple.clinic.user.resetpin

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.forgotpin.ForgotPinResponse
import org.simple.clinic.forgotpin.ResetPinRequest
import org.simple.clinic.login.UsersApi
import org.simple.clinic.security.pin.JavaHashPasswordHasher
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.UserStatus.WaitingForApproval
import org.simple.clinic.user.resetpin.ResetPinResult.NetworkError
import org.simple.clinic.user.resetpin.ResetPinResult.Success
import org.simple.clinic.user.resetpin.ResetPinResult.UnexpectedError
import org.simple.clinic.user.resetpin.ResetPinResult.UserNotFound
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.httpErrorResponse
import org.simple.clinic.util.toPayload
import java.io.IOException
import java.util.UUID

class ResetUserPinTest {

  private val passwordHasher = JavaHashPasswordHasher()
  private val loginApi = mock<UsersApi>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userDao = mock<User.RoomDao>()
  private val accessTokenPref = mock<Preference<Optional<String>>>()
  private val facilityUuid = UUID.fromString("4ffa1d2b-f023-4239-91ad-7fb7ddfddaab")
  private val currentUser = TestData.loggedInUser(
      uuid = UUID.fromString("36f6072c-0757-43e6-9a09-2bb9971cc7d3"),
      pinDigest = hash("0000"),
      loggedInStatus = RESETTING_PIN,
      currentFacilityUuid = facilityUuid,
      registrationFacilityUuid = facilityUuid
  )
  private val newPin = "1234"
  private val newPinDigest = hash(newPin)
  private val updatedUser = currentUser.afterPinResetRequested(newPinDigest)

  private val resetUserPin = ResetUserPin(passwordHasher, loginApi, userDao, facilityRepository, accessTokenPref)

  @Test
  fun `when reset PIN request is raised, the network call must be made with the hashed PIN`() {
    val newPinDigest = hash(newPin)
    val updatedUser = currentUser.afterPinResetRequested(newPinDigest)

    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))
    whenever(facilityRepository.setCurrentFacility(facilityUuid)) doReturn Completable.complete()

    whenever(loginApi.resetPin(ResetPinRequest(newPinDigest))) doReturn Single.just(ForgotPinResponse(
        accessToken = "",
        loggedInUser = updatedUser.toPayload(facilityUuid)
    ))

    resetUserPin.resetPin(newPin).blockingGet()

    verify(loginApi).resetPin(ResetPinRequest(newPinDigest))
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
    whenever(facilityRepository.setCurrentFacility(facilityUuid)) doReturn Completable.complete()

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
    whenever(facilityRepository.setCurrentFacility(facilityUuid)) doReturn Completable.complete()

    val response = ForgotPinResponse(
        loggedInUser = updatedUser.toPayload(facilityUuid),
        accessToken = "new_access_token"
    )
    whenever(loginApi.resetPin(ResetPinRequest(newPinDigest))) doReturn Single.just(response)

    resetUserPin.resetPin(newPin).blockingGet()

    verify(userDao).createOrUpdate(updatedUser)
  }

  @Test
  fun `when the reset PIN call succeeds, the success result must be returned`() {
    // given
    whenever(facilityRepository.setCurrentFacility(facilityUuid)) doReturn Completable.complete()
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))

    val response = ForgotPinResponse(
        loggedInUser = updatedUser.toPayload(facilityUuid),
        accessToken = "new_access_token"
    )
    whenever(loginApi.resetPin(ResetPinRequest(newPinDigest))) doReturn Single.just(response)

    // when
    val result = resetUserPin.resetPin(newPin).blockingGet()

    // then
    assertThat(result).isEqualTo(Success)
  }

  @Test
  fun `when the reset PIN api call fails with a network error, the network error result must be returned`() {
    // given
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))

    val newPinDigest = hash(newPin)
    whenever(loginApi.resetPin(ResetPinRequest(newPinDigest))) doReturn Single.error<ForgotPinResponse>(IOException())

    // when
    val result = resetUserPin.resetPin(newPin).blockingGet()

    // then
    assertThat(result).isEqualTo(NetworkError)
  }

  @Test
  fun `when the reset PIN api call fails with 401 unauthenticated, the user not found result must be returned`() {
    // given
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))

    val newPinDigest = hash(newPin)
    // TODO(vs): 2019-11-22 This is coupled to the implementation. Refactor later.
    val httpException = httpErrorResponse(401)
    whenever(loginApi.resetPin(ResetPinRequest(newPinDigest))) doReturn Single.error<ForgotPinResponse>(httpException)

    // when
    val result = resetUserPin.resetPin(newPin).blockingGet()

    // then
    assertThat(result).isEqualTo(UserNotFound)
  }

  @Test
  fun `when the reset PIN api call fails with a server error, the unexpected error result must be returned`() {
    // given
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))

    val newPinDigest = hash(newPin)
    // TODO(vs): 2019-11-22 This is coupled to the implementation. Refactor later.
    val httpException = httpErrorResponse(500)
    whenever(loginApi.resetPin(ResetPinRequest(newPinDigest))) doReturn Single.error<ForgotPinResponse>(httpException)

    // when
    val result = resetUserPin.resetPin(newPin).blockingGet()

    // then
    assertThat(result).isEqualTo(UnexpectedError(httpException))
  }

  @Test
  fun `when the reset PIN api call fails with any other error, the unexpected error result must be returned`() {
    // given
    whenever(userDao.user()) doReturn Flowable.just(listOf(currentUser))

    val newPinDigest = hash(newPin)
    val exception = RuntimeException()
    whenever(loginApi.resetPin(ResetPinRequest(newPinDigest))) doReturn Single.error<ForgotPinResponse>(exception)

    // when
    val result = resetUserPin.resetPin(newPin).blockingGet()

    // then
    assertThat(result).isEqualTo(UnexpectedError(exception))
  }

  private fun hash(pin: String): String = passwordHasher.hash(pin)

  private fun User.afterPinResetRequested(updatedPinDigest: String): User {
    return copy(pinDigest = updatedPinDigest, status = WaitingForApproval, loggedInStatus = RESET_PIN_REQUESTED)
  }
}
