package org.simple.clinic.enterotp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.login.LoginResult

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
  }
}

@Parcelize
object NetworkError : AsyncOpError()

@Parcelize
data class ServerError(val errorMessage: String) : AsyncOpError()

@Parcelize
object OtherError : AsyncOpError()
