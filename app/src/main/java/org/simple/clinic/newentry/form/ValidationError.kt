package org.simple.clinic.newentry.form

sealed class ValidationError {
  data object MissingValue : ValidationError()
  data object InvalidDateFormat : ValidationError()
  data object DateIsInFuture : ValidationError()
  data object LengthTooShort : ValidationError()
  data object LengthTooLong : ValidationError()
}
