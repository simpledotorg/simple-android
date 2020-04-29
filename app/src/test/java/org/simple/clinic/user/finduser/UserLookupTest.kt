package org.simple.clinic.user.finduser

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.FakeCall
import org.simple.clinic.login.UsersApi
import org.simple.clinic.registration.FindUserRequest
import org.simple.clinic.registration.FindUserResponse
import org.simple.clinic.registration.FindUserResponse.Body
import org.simple.clinic.user.UserStatus
import org.simple.clinic.user.finduser.FindUserResult.Found
import org.simple.clinic.user.finduser.FindUserResult.NetworkError
import org.simple.clinic.user.finduser.FindUserResult.NotFound
import org.simple.clinic.user.finduser.FindUserResult.UnexpectedError
import org.simple.clinic.util.RxErrorsRule
import java.net.SocketTimeoutException
import java.util.UUID

class UserLookupTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val phoneNumber = "1234567890"

  private val usersApi = mock<UsersApi>()

  private val findUserWithPhoneNumber = UserLookup(usersApi)

  @Test
  fun `when the find user by phone number call succeeds, return the fetched data`() {
    // given
    val userUuid = UUID.fromString("70036d9e-6b5e-4166-9682-594647c32f26")
    val userStatus = UserStatus.WaitingForApproval
    whenever(usersApi.findUserByPhoneNumber(FindUserRequest(phoneNumber)))
        .doReturn(FakeCall.success(FindUserResponse(Body(userUuid, userStatus))))

    // when
    val result = findUserWithPhoneNumber.find(phoneNumber)

    // then
    assertThat(result).isEqualTo(Found(userUuid, userStatus))
  }

  @Test
  fun `when the find user by phone number call returns 404, return the not found result`() {
    // given
    whenever(usersApi.findUserByPhoneNumber(FindUserRequest(phoneNumber)))
        .doReturn(FakeCall.error("", responseCode = 404))

    // when
    val result = findUserWithPhoneNumber.find(phoneNumber)

    // then
    assertThat(result).isEqualTo(NotFound)
  }

  @Test
  fun `when the find user by phone number call fails with a network error, return the network error result`() {
    // given
    whenever(usersApi.findUserByPhoneNumber(FindUserRequest(phoneNumber)))
        .doReturn(FakeCall.failure(SocketTimeoutException()))

    // when
    val result = findUserWithPhoneNumber.find(phoneNumber)

    // then
    assertThat(result).isEqualTo(NetworkError)
  }

  @Test
  fun `when the find user by phone number call returns a server error, return the fallback error result`() {
    // given
    whenever(usersApi.findUserByPhoneNumber(FindUserRequest(phoneNumber)))
        .doReturn(FakeCall.error("", responseCode = 500))

    // when
    val result = findUserWithPhoneNumber.find(phoneNumber)

    // then
    assertThat(result).isEqualTo(UnexpectedError)
  }
}
