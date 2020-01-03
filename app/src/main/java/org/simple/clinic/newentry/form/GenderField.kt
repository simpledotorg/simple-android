package org.simple.clinic.newentry.form

import org.simple.clinic.newentry.form.ValidationError.MissingValue
import org.simple.clinic.patient.Gender

class GenderField(labelResId: Int) : InputField<Gender?>(labelResId) {
  override fun validate(value: Gender?): Set<ValidationError> {
    return if (value == null) setOf(MissingValue) else emptySet()
  }
}
