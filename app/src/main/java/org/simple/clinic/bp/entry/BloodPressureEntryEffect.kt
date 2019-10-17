package org.simple.clinic.bp.entry

import java.util.UUID

sealed class BloodPressureEntryEffect

object PrefillDateForNewEntry : BloodPressureEntryEffect()

object HideBpErrorMessage : BloodPressureEntryEffect()

object ChangeFocusToDiastolic : BloodPressureEntryEffect()

object ChangeFocusToSystolic : BloodPressureEntryEffect()

data class SetSystolic(val systolic: String) : BloodPressureEntryEffect()

data class FetchBloodPressureMeasurement(
    val bpUuid: UUID
) : BloodPressureEntryEffect()

data class SetDiastolic(val diastolic: String) : BloodPressureEntryEffect()

data class ShowConfirmRemoveBloodPressureDialog(
    val bpUuid: UUID
) : BloodPressureEntryEffect()
