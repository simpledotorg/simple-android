package org.simple.clinic.user.finduser

import io.reactivex.Single
import org.simple.clinic.registration.RegistrationApi
import org.simple.clinic.user.finduser.FindUserResult.Found_Old
import org.simple.clinic.user.finduser.FindUserResult.NetworkError
import org.simple.clinic.user.finduser.FindUserResult.NotFound
import org.simple.clinic.user.finduser.FindUserResult.UnexpectedError
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
}
