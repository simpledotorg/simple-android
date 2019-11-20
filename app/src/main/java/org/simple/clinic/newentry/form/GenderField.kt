package org.simple.clinic.newentry.form

import org.simple.clinic.newentry.form.ValidationError.FieldIsBlankEmpty
import org.simple.clinic.patient.Gender

class GenderField : InputField<Gender?>() {
  override fun validate(value: Gender?): List<ValidationError> {
    return if (value == null) listOf(FieldIsBlankEmpty) else emptyList()
  }
}
