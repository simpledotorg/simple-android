package org.simple.clinic.newentry.form

import org.simple.clinic.newentry.form.ValidationError.MissingValue

class PatientNameField : InputField<String>() {
  override fun validate(value: String): List<ValidationError> {
    if (value.isNotBlank()) {
      return emptyList()
    }
    return listOf(MissingValue)
  }
}
