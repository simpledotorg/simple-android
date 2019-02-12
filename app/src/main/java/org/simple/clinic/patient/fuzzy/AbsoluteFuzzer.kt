package org.simple.clinic.patient.fuzzy

import org.simple.clinic.util.UtcClock
import org.threeten.bp.LocalDate

/**
 * This fuzzes age by a fixed ± value from the passed in age.
 **/
class AbsoluteFuzzer(private val utcClock: UtcClock, private val fuzziness: Int): AgeFuzzer {

  init {
    if (fuzziness < 0) {
      throw AssertionError("Fuzziness cannot be negative!")
    }
  }

  override fun bounded(age: Int): BoundedAge {
    if (age < 0) {
      throw AssertionError("Age cannot be negative")
    }
    val today = LocalDate.now(utcClock)

    val dateLowerBound = today.minusYears((age + fuzziness).toLong())
    val dateUpperBound = today.minusYears((age - fuzziness).toLong())

    return BoundedAge(lower = dateLowerBound, upper = dateUpperBound)
  }
}
