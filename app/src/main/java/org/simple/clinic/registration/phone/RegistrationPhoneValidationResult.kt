package org.simple.clinic.registration.phone

sealed class RegistrationPhoneValidationResult {

  object Valid : RegistrationPhoneValidationResult()

  sealed class Invalid : RegistrationPhoneValidationResult() {

    object TooShort : Invalid()

    object TooLong : Invalid()

    object Blank : Invalid()
  }
}
