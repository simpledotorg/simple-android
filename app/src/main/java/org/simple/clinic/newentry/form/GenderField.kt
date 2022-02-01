package org.simple.clinic.newentry.form

import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.Gender

@Parcelize
data class GenderField(
    private val _labelResId: Int,
    val allowedGenders: Set<Gender>
) : InputField<Gender?>(_labelResId)
