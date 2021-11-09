package org.simple.clinic.user.finduser

import org.simple.clinic.DEMO_USER_ID
import org.simple.clinic.DEMO_USER_PHONE_NUMBER
import org.simple.clinic.login.UsersApi
import org.simple.clinic.registration.FindUserRequest
import org.simple.clinic.user.UserStatus
import org.simple.clinic.user.finduser.FindUserResult.Found
import org.simple.clinic.user.finduser.FindUserResult.NetworkError
import org.simple.clinic.user.finduser.FindUserResult.NotFound
import org.simple.clinic.user.finduser.FindUserResult.UnexpectedError
import org.simple.clinic.util.ErrorResolver
import org.simple.clinic.util.ResolvedError.NetworkRelated
import timber.log.Timber
import javax.inject.Inject

class UserLookup @Inject constructor(
    private val usersApi: UsersApi
) {

  fun find(phoneNumber: String): FindUserResult {
    if (phoneNumber == DEMO_USER_PHONE_NUMBER) {
      Timber.i("Demo user found")
      return Found(DEMO_USER_ID, UserStatus.WaitingForApproval)
    }
    Timber.i("Finding user with phone number")
    return try {
      makeFindUserApiCall(phoneNumber)
    } catch (e: Throwable) {
      when (ErrorResolver.resolve(e)) {
        is NetworkRelated -> NetworkError
        else -> UnexpectedError
      }
    }
  }

  private fun makeFindUserApiCall(phoneNumber: String): FindUserResult {
    val request = FindUserRequest(phoneNumber)
    val response = usersApi.findUserByPhoneNumber(request).execute()

    return when (val code = response.code()) {
      200 -> {
        val responseData = response.body()!!.body

        Found(responseData.userUuid, responseData.status)
      }
      404 -> NotFound
      else -> throw RuntimeException("Not an expected response code: $code")
    }
  }
}
