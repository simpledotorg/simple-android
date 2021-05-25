package org.simple.clinic.login.activateuser

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.simple.clinic.FakeCall
import org.simple.clinic.TestData
import org.simple.clinic.login.LoginOtpSmsListener
import org.simple.clinic.login.UsersApi
import org.simple.clinic.login.activateuser.ActivateUser.Result.IncorrectPin
import org.simple.clinic.login.activateuser.ActivateUser.Result.NetworkError
import org.simple.clinic.login.activateuser.ActivateUser.Result.OtherError
import org.simple.clinic.login.activateuser.ActivateUser.Result.ServerError
import org.simple.clinic.login.activateuser.ActivateUser.Result.Success
import java.net.SocketTimeoutException
import java.util.UUID

class ActivateUserTest {

  private val api = mock<UsersApi>()
  private val loginOtpSmsListener = mock<LoginOtpSmsListener>()

  private val userUuid = UUID.fromString("a2be9a8f-8aab-4ecb-b4d2-9ed74bfbd800")

  private val correctPin = "1234"
  private val incorrectPin = "1111"

  private val activateUser = ActivateUser(api, loginOtpSmsListener)

  @Test
  fun `when the activate request is successful, return the success result`() {
    // given
    val userPayload = TestData.loggedInUserPayload(uuid = userUuid)

    whenever(api.activate(ActivateUserRequest.create(userUuid, correctPin))) doReturn FakeCall.success(ActivateUserResponse(userPayload))

    // when
    val result = activateUser.activate(userUuid, correctPin)

    // then
    assertThat(result).isEqualTo(Success(userPayload))
  }

  @Test
  fun `when the activate request returns a status code of 401, return the incorrect pin result`() {
    // given
    whenever(api.activate(ActivateUserRequest.create(userUuid, incorrectPin))) doReturn FakeCall.error("", responseCode = 401)

    // when
    val result = activateUser.activate(userUuid, incorrectPin)

    // then
    assertThat(result).isEqualTo(IncorrectPin)
  }

  @Test
  fun `when the activate request returns any other status code, return the server error result`() {
    // given
    whenever(api.activate(ActivateUserRequest.create(userUuid, correctPin))) doReturn FakeCall.error("", responseCode = 500)

    // when
    val result = activateUser.activate(userUuid, correctPin)

    // then
    assertThat(result).isEqualTo(ServerError(responseCode = 500))
  }

  @Test
  fun `when the activate request fails with a network error, return the network error result`() {
    // given
    whenever(api.activate(ActivateUserRequest.create(userUuid, correctPin))) doReturn FakeCall.failure(SocketTimeoutException())

    // when
    val result = activateUser.activate(userUuid, correctPin)

    // then
    assertThat(result).isEqualTo(NetworkError)
  }

  @Test
  fun `when the activate request fails with any other error, return the other error result`() {
    // given
    val cause = RuntimeException()
    whenever(api.activate(ActivateUserRequest.create(userUuid, correctPin))) doReturn FakeCall.failure(cause)

    // when
    val result = activateUser.activate(userUuid, correctPin)

    // then
    assertThat(result).isEqualTo(OtherError(cause))
  }

  @Test
  fun `before the call to activate the user is made, listening for the login OTP SMS must be done`() {
    // given
    val inOrder = inOrder(loginOtpSmsListener, api)

    // when
    activateUser.activate(userUuid, correctPin)

    // then
    inOrder.verify(loginOtpSmsListener).listenForLoginOtp()
    inOrder.verify(api).activate(ActivateUserRequest.create(userUuid, correctPin))
    inOrder.verifyNoMoreInteractions()
  }
}
