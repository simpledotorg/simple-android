package org.simple.clinic.user.registeruser

sealed class RegistrationResult {

  data object Success : RegistrationResult()

  data object UnexpectedError : RegistrationResult()

  data object NetworkError : RegistrationResult()
}
