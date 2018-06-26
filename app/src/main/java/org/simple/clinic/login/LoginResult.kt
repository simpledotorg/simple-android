package org.simple.clinic.login

sealed class LoginResult {

  class Success : LoginResult()

  class NetworkError : LoginResult()

  class ServerError : LoginResult()
}
