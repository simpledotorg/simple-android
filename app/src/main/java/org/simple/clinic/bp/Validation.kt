package org.simple.clinic.bp

// TODO: Rename to "Result".
sealed class Validation {
  // TODO: Rename to "Valid".
  data class Success(val reading: BloodPressureReading) : Validation()

  object ErrorSystolicEmpty : Validation()
  object ErrorDiastolicEmpty : Validation()
  object ErrorSystolicTooHigh : Validation()
  object ErrorSystolicTooLow : Validation()
  object ErrorDiastolicTooHigh : Validation()
  object ErrorDiastolicTooLow : Validation()
  object ErrorSystolicLessThanDiastolic : Validation()
}
