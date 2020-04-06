package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.HbA1c
import org.simple.clinic.bloodsugar.entry.ValidationResult.ErrorBloodSugarEmpty
import org.simple.clinic.bloodsugar.entry.ValidationResult.ErrorBloodSugarTooHigh
import org.simple.clinic.bloodsugar.entry.ValidationResult.ErrorBloodSugarTooLow
import org.simple.clinic.bloodsugar.entry.ValidationResult.Valid
import javax.inject.Inject

class BloodSugarValidator @Inject constructor() {

  fun validate(bloodSugarReading: BloodSugarReading): ValidationResult {
    val value = bloodSugarReading.value.trim()
    val type = bloodSugarReading.type

    if (value.isBlank()) {
      return ErrorBloodSugarEmpty
    }

    val bloodSugarNumber = value.toFloat()
    val minAllowedBloodSugarValue = if (type is HbA1c) 3 else 30
    val maxAllowedBloodSugarValue = if (type is HbA1c) 25 else 1000

    return when {
      bloodSugarNumber < minAllowedBloodSugarValue -> ErrorBloodSugarTooLow(type)
      bloodSugarNumber > maxAllowedBloodSugarValue -> ErrorBloodSugarTooHigh(type)
      else -> Valid(bloodSugarNumber)
    }
  }
}
