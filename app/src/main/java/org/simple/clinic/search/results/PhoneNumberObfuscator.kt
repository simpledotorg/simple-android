package org.simple.clinic.search.results

import javax.inject.Inject

abstract class PhoneNumberObfuscator(val digitsVisibleOnTheLeft: Int, val digitsVisibleOnTheRight: Int) {

  fun obfuscate(number: String): String {
    // TODO: Make this work.
    return number
  }
}

class IndianPhoneNumberObfuscator @Inject constructor() : PhoneNumberObfuscator(digitsVisibleOnTheLeft = 2, digitsVisibleOnTheRight = 3)
