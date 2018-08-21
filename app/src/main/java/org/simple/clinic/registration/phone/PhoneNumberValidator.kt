package org.simple.clinic.registration.phone

interface PhoneNumberValidator {
  fun isValid(number: String): Boolean
}

class IndianPhoneNumberValidator : PhoneNumberValidator {

  override fun isValid(number: String): Boolean {
    return number.length == 10
  }
}
