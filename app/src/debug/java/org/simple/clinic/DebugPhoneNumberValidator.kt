package org.simple.clinic

import org.simple.clinic.registration.phone.PhoneNumberValidator

class DebugPhoneNumberValidator : PhoneNumberValidator {

  override fun isValid(number: String): Boolean {
    // "0000" is our test phone number.
    return number.length >= 4
  }
}
