package org.simple.clinic.registration

sealed class RegistrationResult {

  object Success : RegistrationResult()

  object UnexpectedError : RegistrationResult()

  object NetworkError : RegistrationResult()
}
