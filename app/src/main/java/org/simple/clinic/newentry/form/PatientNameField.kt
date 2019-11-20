package org.simple.clinic.newentry.form

import org.simple.clinic.newentry.form.ValidationError.FieldIsBlankEmpty

class PatientNameField : InputField() {
  fun validate(value: String): List<ValidationError> {
    if (value.isNotBlank()) {
      return emptyList()
    }
    return listOf(FieldIsBlankEmpty)
  }
}
