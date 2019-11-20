package org.simple.clinic.newentry.form

abstract class InputField<T> {
  abstract fun validate(value: T): List<ValidationError>
}
