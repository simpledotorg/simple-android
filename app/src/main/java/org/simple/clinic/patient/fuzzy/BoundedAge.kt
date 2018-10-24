package org.simple.clinic.patient.fuzzy

import org.threeten.bp.LocalDate

data class BoundedAge(val lower: LocalDate, val upper: LocalDate) {

  init {
    if (upper < lower) {
      throw AssertionError("Upper bound [$upper] should be >= lower bound [$lower]")
    }
  }
}
