package org.simple.clinic.patient.fuzzy

data class BoundedAge(val lower: Int, val upper: Int) {
  init {
    if (upper < lower) {
      throw AssertionError("Upper bound [$upper] should be >= lower bound [$lower]")
    }
  }
}
