package org.simple.clinic.patient.fuzzy

/**
 * This fuzzes age by a fixed ± value from the passed in age.
 **/
class AbsoluteFuzzer(private val fuzziness: Int) : AgeFuzzer {

  init {
    if (fuzziness < 0) {
      throw AssertionError("Fuzziness cannot be negative!")
    }
  }

  override fun bounded(age: Int): BoundedAge {
    if (age < 0) {
      throw AssertionError("Age cannot be negative")
    }
    return BoundedAge((age - fuzziness).coerceAtLeast(0), age + fuzziness)
  }
}
