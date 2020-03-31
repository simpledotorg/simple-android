package org.simple.clinic.user.finduser

import io.reactivex.Single
import org.simple.clinic.registration.FindUserRequest
import org.simple.clinic.registration.RegistrationApi
import org.simple.clinic.user.finduser.FindUserResult.Found
import org.simple.clinic.user.finduser.FindUserResult.Found_Old
import org.simple.clinic.user.finduser.FindUserResult.NetworkError
import org.simple.clinic.user.finduser.FindUserResult.NotFound
import org.simple.clinic.user.finduser.FindUserResult.UnexpectedError
import org.simple.clinic.util.ErrorResolver
import org.simple.clinic.util.ResolvedError.NetworkRelated
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class UserLookup @Inject constructor(
    private val registrationApi: RegistrationApi
) {

  // TODO: Remove this once we move to the new user refresh call
  fun find_old(phoneNumber: String): Single<FindUserResult> {
    Timber.i("Finding user with phone number")
    return registrationApi.findUser(phoneNumber)
        .map { user -> Found_Old(user) as FindUserResult }
        .onErrorReturn(::mapErrorToResult)
  }

  private fun mapErrorToResult(error: Throwable): FindUserResult {
    return when {
      error is IOException -> NetworkError
      error is HttpException && error.code() == 404 -> NotFound
      else -> {
        Timber.e(error)
        UnexpectedError
      }
    }
  }

  fun find(phoneNumber: String): FindUserResult {
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
    val response = registrationApi.findUserByPhoneNumber(request).execute()

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
