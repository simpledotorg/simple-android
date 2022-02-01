package org.simple.clinic.newentry.form

import kotlinx.parcelize.Parcelize

@Parcelize
data class AgeField(
    private val _labelResId: Int
) : InputField<String>(_labelResId)
