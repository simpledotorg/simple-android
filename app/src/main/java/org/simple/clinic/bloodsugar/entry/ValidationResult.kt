package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.BloodSugarMeasurementType

sealed class ValidationResult {
  data class Valid(val bloodSugarReading: Float) : ValidationResult()

  object ErrorBloodSugarEmpty : ValidationResult()
  data class ErrorBloodSugarTooHigh(val measurementType: BloodSugarMeasurementType) : ValidationResult()
  data class ErrorBloodSugarTooLow(val measurementType: BloodSugarMeasurementType) : ValidationResult()
}
