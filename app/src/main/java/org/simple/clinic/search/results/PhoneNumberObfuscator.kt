package org.simple.clinic.search.results

import javax.inject.Inject

interface PhoneNumberObfuscator {
  fun obfuscate(number: String): String
}

class IndianPhoneNumberObfuscator @Inject constructor() : PhoneNumberObfuscator {
  override fun obfuscate(number: String): String {
    if (number.length < 10) {
      return "• ".repeat(number.length)
    }

    val first2Digits = number.substring(0, 2)
    val last3Digits = number.substring(number.length - 3, number.length)
    val obfuscatedDigits = "• ".repeat(number.length - 5)
    return "$first2Digits $obfuscatedDigits$last3Digits"
  }
}
