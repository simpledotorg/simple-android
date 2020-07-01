package org.simple.clinic.registration.register

import org.simple.clinic.user.registeruser.RegistrationResult

enum class RegisterUserResult {
  Success,
  NetworkError,
  OtherError;

  companion object {
    fun from(result: RegistrationResult): RegisterUserResult {
      return when (result) {
        RegistrationResult.Success -> Success
        RegistrationResult.UnexpectedError -> OtherError
        RegistrationResult.NetworkError -> NetworkError
      }
    }
  }
}
