package org.simple.clinic.bp.entry

import org.threeten.bp.Instant
import java.util.UUID

sealed class BloodPressureEntryEffect

data class PrefillDate(val date: Instant? = null) : BloodPressureEntryEffect() {
  companion object {
    fun forNewEntry(): PrefillDate {
      return PrefillDate()
    }

    fun forUpdateEntry(date: Instant): PrefillDate {
      return PrefillDate(date)
    }
  }
}

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

object Dismiss : BloodPressureEntryEffect()

object HideDateErrorMessage : BloodPressureEntryEffect()
