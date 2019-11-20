package org.simple.clinic.newentry.form

import org.simple.clinic.newentry.form.ValidationError.MissingValue

class StreetAddressField : InputField<String>() {
  override fun validate(value: String): List<ValidationError> {
    return if (value.isBlank()) listOf(MissingValue) else emptyList()
  }
}
