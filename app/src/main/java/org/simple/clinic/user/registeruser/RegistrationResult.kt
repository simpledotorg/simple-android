package org.simple.clinic.user.registeruser

sealed class RegistrationResult {

  object Success : RegistrationResult()

  object UnexpectedError : RegistrationResult()

  object NetworkError : RegistrationResult()
}
