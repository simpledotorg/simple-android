package org.simple.clinic.login

sealed class LoginResult {

  class Success : LoginResult()

  class NetworkError : LoginResult()

  data class ServerError(val error: String) : LoginResult()

  class UnexpectedError : LoginResult()
}
