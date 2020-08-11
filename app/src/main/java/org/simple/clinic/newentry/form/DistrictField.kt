package org.simple.clinic.newentry.form

import org.simple.clinic.newentry.form.ValidationError.MissingValue

data class DistrictField(
    private val _labelResId: Int
) : InputField<String>(_labelResId) {
  override fun validate(value: String): Set<ValidationError> {
    return if (value.isBlank()) setOf(MissingValue) else emptySet()
  }
}
