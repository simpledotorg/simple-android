package org.simple.clinic.login

sealed class LoginResult {

  object Success : LoginResult()

  object NetworkError : LoginResult()

  data class ServerError(val error: String) : LoginResult()

  object UnexpectedError : LoginResult()
}
