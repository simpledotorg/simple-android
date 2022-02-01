package org.simple.clinic.newentry.form

import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class DateOfBirthField(
    private val parseDate: (String) -> LocalDate,
    private val today: LocalDate,
    private val _labelResId: Int
) : InputField<String>(_labelResId)
