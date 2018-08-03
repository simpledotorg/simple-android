package org.simple.clinic.registration.phone

import javax.inject.Inject

class PhoneNumberValidator @Inject constructor() {

  val LENGTH_OF_INDIAN_PHONE_NUMBER = 10

  fun isValid(number: String): Boolean {
    // Only Indian phone numbers are accepted for now.
    return number.length == LENGTH_OF_INDIAN_PHONE_NUMBER
  }
}
