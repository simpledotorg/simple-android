package org.simple.clinic.newentry.form

import kotlinx.parcelize.Parcelize

@Parcelize
data class StateField(
    private val _labelResId: Int
) : InputField<String>(_labelResId)
