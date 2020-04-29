package org.simple.clinic.newentry.form

sealed class ValidationError {
  object MissingValue : ValidationError()
  object InvalidDateFormat : ValidationError()
  object DateIsInFuture : ValidationError()
  object LengthTooShort : ValidationError()
  object LengthTooLong : ValidationError()
}
