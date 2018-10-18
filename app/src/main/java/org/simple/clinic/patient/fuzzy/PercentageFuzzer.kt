package org.simple.clinic.patient.fuzzy

/**
 * This fuzzes age by a percentage of the passed in age.
 **/
class PercentageFuzzer(private val fuzziness: Float) : AgeFuzzer {

  init {
    if (fuzziness < 0) {
      throw AssertionError("Fuzziness cannot be negative!")
    }
    if (fuzziness > 1.0F) {
      throw AssertionError("Fuzziness must be between 0.0 and 1.0")
    }
  }

  override fun bounded(age: Int): BoundedAge {
    if (age < 0) {
      throw AssertionError("Age cannot be negative")
    }
    val ageDelta = age * fuzziness
    return BoundedAge((age - ageDelta).toInt(), (age + ageDelta).toInt())
  }
}
