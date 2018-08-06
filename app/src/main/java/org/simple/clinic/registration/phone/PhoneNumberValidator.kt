package org.simple.clinic.registration.phone

import javax.inject.Inject

private const val LENGTH_OF_INDIAN_PHONE_NUMBER = 10

class PhoneNumberValidator @Inject constructor() {

  fun isValid(number: String): Boolean {
    return number.length == LENGTH_OF_INDIAN_PHONE_NUMBER
  }
}
