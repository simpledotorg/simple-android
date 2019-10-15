package org.simple.clinic.bp.entry

import org.threeten.bp.LocalDate

sealed class BloodPressureEntryEffect

data class PrefillDate(
    val date: LocalDate
) : BloodPressureEntryEffect()
