package org.simple.clinic.newentry.form

import kotlinx.parcelize.Parcelize

@Parcelize
data class DistrictField(
    private val _labelResId: Int
) : InputField<String>(_labelResId)
