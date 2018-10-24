package org.simple.clinic.patient.fuzzy

import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import kotlin.math.floor

/**
 * This fuzzes age by a percentage of the passed in age.
 **/
class PercentageFuzzer(private val clock: Clock, private val fuzziness: Float) : AgeFuzzer {

  init {
    if (fuzziness < 0F) {
      throw AssertionError("Fuzziness cannot be negative!")
    }
    if (fuzziness > 1F) {
      throw AssertionError("Fuzziness must be between 0.0 and 1.0")
    }
  }

  private val oneYearAsDays = 365.25F

  override fun bounded(age: Int): BoundedAge {
    if (age <= 0) {
      throw AssertionError("Age cannot be negative or zero")
    }
    val dateToFuzz = LocalDate.now(clock).minusYears(age.toLong())

    val ageDelta = age * fuzziness

    val daysDelta = floor(ageDelta * oneYearAsDays).toLong()

    val dateLowerBound = dateToFuzz.minusDays(daysDelta)
    val dateUpperBound = dateToFuzz.plusDays(daysDelta)

    return BoundedAge(dateLowerBound, dateUpperBound)
  }
}
