package org.simple.clinic.user

import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.login.LoginApi
import org.simple.clinic.login.LoginOtpSmsListener
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID

class RequestLoginOtp(
    private val loginApi: LoginApi,
    private val loginOtpSmsListener: LoginOtpSmsListener
) {

  fun requestForUser(userUuid: UUID): Single<Result> {
    return listenForLoginSms()
        .andThen(makeRequestOtpNetworkCall(userUuid))
  }

  private fun listenForLoginSms(): Completable {
    return loginOtpSmsListener
        .listenForLoginOtp()
        .onErrorComplete()
  }

  private fun makeRequestOtpNetworkCall(userUuid: UUID): Single<Result> {
    return loginApi.requestLoginOtp(userUuid)
        .andThen(Single.just(Result.Success as Result))
        .onErrorReturn { cause ->
          when (cause) {
            is HttpException -> Result.ServerError(cause.code())
            is IOException -> Result.NetworkError
            else -> Result.OtherError(cause)
          }
        }
  }

  sealed class Result {

    object Success : Result()

    object NetworkError : Result()

    data class ServerError(val responseCode: Int) : Result()

    data class OtherError(val cause: Throwable) : Result()
  }
}
