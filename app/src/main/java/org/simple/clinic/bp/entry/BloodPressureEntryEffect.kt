package org.simple.clinic.bp.entry

import org.simple.clinic.bp.entry.BpValidator.Validation
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
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

data class ShowBpValidationError(val result: Validation) : BloodPressureEntryEffect()

object ShowDateEntryScreen : BloodPressureEntryEffect()

data class ShowBpEntryScreen(val date: LocalDate) : BloodPressureEntryEffect()

data class ShowDateValidationError(
    val result: Result
) : BloodPressureEntryEffect()

data class CreateNewBpEntry(
    val patientUuid: UUID,
    val systolic: Int,
    val diastolic: Int,
    val date: LocalDate
) : BloodPressureEntryEffect()

object SetBpSavedResultAndFinish : BloodPressureEntryEffect()

data class UpdateBpEntry(
    val bpUuid: UUID,
    val systolic: Int,
    val diastolic: Int,
    val date: LocalDate
) : BloodPressureEntryEffect()
