package org.simple.clinic.enterotp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.login.LoginResult

sealed class LoginError : Parcelable {

  companion object {
    fun from(loginResult: LoginResult): LoginError {
      return when (loginResult) {
        LoginResult.NetworkError -> NetworkError
        is LoginResult.ServerError -> ServerError(loginResult.error)
        LoginResult.UnexpectedError -> OtherError
        else -> throw IllegalArgumentException("$loginResult is not a valid error case!")
      }
    }
  }
}

@Parcelize
object NetworkError : LoginError()

@Parcelize
data class ServerError(val errorMessage: String) : LoginError()

@Parcelize
object OtherError : LoginError()
