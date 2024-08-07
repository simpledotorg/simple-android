package org.simple.clinic.bp.entry

import org.simple.clinic.bp.BloodPressureReading
import org.simple.clinic.bp.ValidationResult
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

sealed class BloodPressureEntryEffect

sealed class PrefillDate : BloodPressureEntryEffect() {
  companion object {
    fun forNewEntry(): PrefillDate {
      return PrefillCurrentDate
    }

    fun forUpdateEntry(date: Instant): PrefillDate {
      return PrefillSpecificDate(date)
    }
  }

  data object PrefillCurrentDate : PrefillDate()

  data class PrefillSpecificDate(val date: Instant) : PrefillDate()
}

data object HideBpErrorMessage : BloodPressureEntryEffect()

data object ChangeFocusToDiastolic : BloodPressureEntryEffect()

data object ChangeFocusToSystolic : BloodPressureEntryEffect()

data class SetSystolic(val systolic: String) : BloodPressureEntryEffect()

data class FetchBloodPressureMeasurement(val bpUuid: UUID) : BloodPressureEntryEffect()

data class SetDiastolic(val diastolic: String) : BloodPressureEntryEffect()

data class ShowConfirmRemoveBloodPressureDialog(val bpUuid: UUID) : BloodPressureEntryEffect()

data object Dismiss : BloodPressureEntryEffect()

data object HideDateErrorMessage : BloodPressureEntryEffect()

data class ShowBpValidationError(val result: ValidationResult) : BloodPressureEntryEffect()

data object ShowDateEntryScreen : BloodPressureEntryEffect()

data class ShowBpEntryScreen(val date: LocalDate) : BloodPressureEntryEffect()

data class ShowDateValidationError(val result: Result) : BloodPressureEntryEffect()

data class CreateNewBpEntry(
    val patientUuid: UUID,
    val reading: BloodPressureReading,
    val userEnteredDate: LocalDate,
    val prefilledDate: LocalDate
) : BloodPressureEntryEffect() {

  val wasDateChanged: Boolean
    get() = userEnteredDate != prefilledDate
}

data object SetBpSavedResultAndFinish : BloodPressureEntryEffect()

data class UpdateBpEntry(
    val bpUuid: UUID,
    val reading: BloodPressureReading,
    val userEnteredDate: LocalDate,
    val prefilledDate: LocalDate
) : BloodPressureEntryEffect() {

  val wasDateChanged: Boolean
    get() = userEnteredDate != prefilledDate
}
