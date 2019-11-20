package org.simple.clinic.newentry.form

sealed class ValidationError {
  object MissingValue : ValidationError()
}
