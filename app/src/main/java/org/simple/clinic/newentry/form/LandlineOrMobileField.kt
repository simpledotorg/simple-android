package org.simple.clinic.newentry.form

import kotlinx.parcelize.Parcelize
import org.simple.clinic.newentry.form.ValidationError.LengthTooLong
import org.simple.clinic.newentry.form.ValidationError.LengthTooShort
import org.simple.clinic.newentry.form.ValidationError.MissingValue

@Parcelize
data class LandlineOrMobileField(
    private val _labelResId: Int
) : InputField<String>(_labelResId) {
  override fun validate(value: String): Set<ValidationError> {
    return when {
      value.isBlank() -> setOf(MissingValue)
      value.length < 6 -> setOf(LengthTooShort)
      value.length > 12 -> setOf(LengthTooLong)
      else -> emptySet()
    }
  }
}
