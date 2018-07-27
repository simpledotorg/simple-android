package org.simple.clinic.registration

sealed class RegistrationResult {

  class Success : RegistrationResult()

  class Error : RegistrationResult()
}
