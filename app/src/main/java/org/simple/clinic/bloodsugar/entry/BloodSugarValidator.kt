package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.BloodSugarReading
import javax.inject.Inject

class BloodSugarValidator @Inject constructor() {

  @Deprecated(
      message = "",
      replaceWith = ReplaceWith("bloodSugarReading.validate()")
  )
  fun validate(bloodSugarReading: BloodSugarReading): ValidationResult {
    return bloodSugarReading.validate()
  }
}
