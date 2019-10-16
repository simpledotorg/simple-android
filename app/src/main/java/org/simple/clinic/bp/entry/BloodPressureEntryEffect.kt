package org.simple.clinic.bp.entry

sealed class BloodPressureEntryEffect

object PrefillDateForNewEntry : BloodPressureEntryEffect()

object HideBpErrorMessage : BloodPressureEntryEffect()

object ChangeFocusToDiastolic : BloodPressureEntryEffect()
