package org.simple.clinic.newentry.form

import kotlinx.parcelize.Parcelize

@Parcelize
data class PatientNameField(
    private val _labelResId: Int
) : InputField<String>(_labelResId)
