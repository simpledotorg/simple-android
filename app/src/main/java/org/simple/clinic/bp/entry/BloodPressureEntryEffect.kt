package org.simple.clinic.bp.entry

sealed class BloodPressureEntryEffect

object PrefillDateForNewEntry : BloodPressureEntryEffect()

object HideBpErrorMessage : BloodPressureEntryEffect()

object ChangeFocusToDiastolic : BloodPressureEntryEffect()

object ChangeFocusToSystolic : BloodPressureEntryEffect()

data class SetSystolic(val systolic: String) : BloodPressureEntryEffect()
