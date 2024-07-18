package org.simple.clinic.login

sealed class LoginResult {

  data object Success : LoginResult()

  data object NetworkError : LoginResult()

  data class ServerError(val error: String) : LoginResult()

  data object UnexpectedError : LoginResult()
}
