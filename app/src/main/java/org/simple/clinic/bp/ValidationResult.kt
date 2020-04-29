package org.simple.clinic.bp

sealed class ValidationResult {
  data class Valid(val reading: BloodPressureReading) : ValidationResult()

  object ErrorSystolicEmpty : ValidationResult()
  object ErrorDiastolicEmpty : ValidationResult()
  object ErrorSystolicTooHigh : ValidationResult()
  object ErrorSystolicTooLow : ValidationResult()
  object ErrorDiastolicTooHigh : ValidationResult()
  object ErrorDiastolicTooLow : ValidationResult()
  object ErrorSystolicLessThanDiastolic : ValidationResult()
}
