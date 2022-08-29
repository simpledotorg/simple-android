package org.simple.clinic.newentry.form

import kotlinx.parcelize.Parcelize

@Parcelize
data class DateOfBirthField(
    private val _labelResId: Int
) : InputField<String>(_labelResId)
