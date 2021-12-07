package org.simple.clinic.newentry.form

import kotlinx.parcelize.Parcelize
import org.simple.clinic.newentry.form.ValidationError.MissingValue
import org.simple.clinic.patient.Gender

@Parcelize
data class GenderField(
    private val _labelResId: Int,
    val allowedGenders: Set<Gender>
) : InputField<Gender?>(_labelResId) {
  override fun validate(value: Gender?): Set<ValidationError> {
    return if (value == null) setOf(MissingValue) else emptySet()
  }
}
