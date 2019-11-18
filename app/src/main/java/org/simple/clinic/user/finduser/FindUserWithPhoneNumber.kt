package org.simple.clinic.user.finduser

import io.reactivex.Single
import org.simple.clinic.registration.RegistrationApi
import org.simple.clinic.user.finduser.FindUserResult.Found
import org.simple.clinic.user.finduser.FindUserResult.NetworkError
import org.simple.clinic.user.finduser.FindUserResult.NotFound
import org.simple.clinic.user.finduser.FindUserResult.UnexpectedError
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class FindUserWithPhoneNumber(private val registrationApi: RegistrationApi) {

  fun find(phoneNumber: String): Single<FindUserResult> {
    Timber.i("Finding user with phone number")
    return registrationApi.findUser(phoneNumber)
        .map { user -> Found(user) as FindUserResult }
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
}
