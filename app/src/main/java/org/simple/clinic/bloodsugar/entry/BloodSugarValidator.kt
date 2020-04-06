package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.HbA1c
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.ValidationResult.ErrorBloodSugarEmpty
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.ValidationResult.ErrorBloodSugarTooHigh
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.ValidationResult.ErrorBloodSugarTooLow
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.ValidationResult.Valid
import javax.inject.Inject

class BloodSugarValidator @Inject constructor() {

  sealed class ValidationResult {
    data class Valid(val bloodSugarReading: Float) : ValidationResult()

    object ErrorBloodSugarEmpty : ValidationResult()
    data class ErrorBloodSugarTooHigh(val measurementType: BloodSugarMeasurementType) : ValidationResult()
    data class ErrorBloodSugarTooLow(val measurementType: BloodSugarMeasurementType) : ValidationResult()
  }

  fun validate(bloodSugarReading: String, measurementType: BloodSugarMeasurementType): ValidationResult {
    if (bloodSugarReading.isBlank()) {
      return ErrorBloodSugarEmpty
    }

    val bloodSugarNumber = bloodSugarReading.trim().toFloat()
    val minAllowedBloodSugarValue = if (measurementType is HbA1c) 3 else 30
    val maxAllowedBloodSugarValue = if (measurementType is HbA1c) 25 else 1000

    return when {
      bloodSugarNumber < minAllowedBloodSugarValue -> ErrorBloodSugarTooLow(measurementType)
      bloodSugarNumber > maxAllowedBloodSugarValue -> ErrorBloodSugarTooHigh(measurementType)
      else -> Valid(bloodSugarNumber)
    }
  }
}
