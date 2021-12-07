package org.simple.clinic.newentry.form

import kotlinx.parcelize.Parcelize
import org.simple.clinic.newentry.form.ValidationError.MissingValue

@Parcelize
data class AgeField(
    private val _labelResId: Int
) : InputField<String>(_labelResId) {
  override fun validate(value: String): Set<ValidationError> {
    return if (value.isBlank()) setOf(MissingValue) else emptySet()
  }
}
