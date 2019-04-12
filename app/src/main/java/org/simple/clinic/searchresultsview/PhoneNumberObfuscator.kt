package org.simple.clinic.searchresultsview

import org.simple.clinic.util.Unicode.bullet
import org.simple.clinic.util.Unicode.hairSpace
import javax.inject.Inject

interface PhoneNumberObfuscator {
  fun obfuscate(number: String): String
}

class IndianPhoneNumberObfuscator @Inject constructor() : PhoneNumberObfuscator {

  private val mask = "$bullet$hairSpace"

  override fun obfuscate(number: String): String {
    val lastThree = (number.lastIndex - 2)..number.lastIndex
    val fiveBeforeLastThree = (lastThree.first - 5)..(lastThree.first - 1)
    val beforeFirstMasked = fiveBeforeLastThree.start - 1

    return number
        .mapIndexed { index, char ->
          when (index) {
            in lastThree -> char.toString()
            in fiveBeforeLastThree -> mask
            beforeFirstMasked -> "$char$hairSpace"
            else -> char.toString()
          }
        }
        .joinToString(separator = "")
  }
}
