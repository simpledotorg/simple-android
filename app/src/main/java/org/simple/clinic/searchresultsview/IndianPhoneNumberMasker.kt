package org.simple.clinic.searchresultsview

import org.simple.clinic.util.Unicode.bullet
import org.simple.clinic.util.Unicode.hairSpace
import javax.inject.Inject

class IndianPhoneNumberMasker @Inject constructor() : PhoneNumberMasker {

  private val mask = "$bullet$hairSpace"

  override fun mask(number: String): String {
    val lastThree = (number.lastIndex - 2)..number.lastIndex
    val fiveBeforeLastThree = (lastThree.first - 5) until lastThree.first
    val beforeFirstMasked = fiveBeforeLastThree.first - 1

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
