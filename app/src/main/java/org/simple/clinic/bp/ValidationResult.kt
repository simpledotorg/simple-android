package org.simple.clinic.bp

sealed class ValidationResult {
  data class Valid(val reading: BloodPressureReading) : ValidationResult()

  data object ErrorSystolicEmpty : ValidationResult()
  data object ErrorDiastolicEmpty : ValidationResult()
  data object ErrorSystolicTooHigh : ValidationResult()
  data object ErrorSystolicTooLow : ValidationResult()
  data object ErrorDiastolicTooHigh : ValidationResult()
  data object ErrorDiastolicTooLow : ValidationResult()
  data object ErrorSystolicLessThanDiastolic : ValidationResult()
}
