package org.simple.clinic.newentry.form

sealed class ValidationError {
  object FieldIsBlankEmpty : ValidationError()
}
