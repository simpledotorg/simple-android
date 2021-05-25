package org.simple.clinic.enterotp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.activateuser.ActivateUser

sealed class AsyncOpError : Parcelable {

  companion object {
    fun from(loginResult: LoginResult): AsyncOpError {
      return when (loginResult) {
        LoginResult.NetworkError -> NetworkError
        is LoginResult.ServerError -> ServerError(loginResult.error)
        LoginResult.UnexpectedError -> OtherError
        else -> throw IllegalArgumentException("$loginResult is not a valid error case!")
      }
    }

    fun from(activateUserResult: ActivateUser.Result): AsyncOpError {
      return when (activateUserResult) {
        ActivateUser.Result.NetworkError -> NetworkError
        is ActivateUser.Result.ServerError, is ActivateUser.Result.OtherError -> OtherError
        else -> throw IllegalArgumentException("$activateUserResult is not a valid error case!")
      }
    }
  }
}

@Parcelize
object NetworkError : AsyncOpError()

@Parcelize
data class ServerError(val errorMessage: String) : AsyncOpError()

@Parcelize
object OtherError : AsyncOpError()
