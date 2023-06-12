package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

sealed class BloodSugarEntryEffect

sealed class PrefillDate : BloodSugarEntryEffect() {
  companion object {
    fun forNewEntry(): PrefillDate {
      return PrefillCurrentDate
    }

    fun forUpdateEntry(date: Instant): PrefillDate {
      return PrefillSpecificDate(date)
    }
  }

  object PrefillCurrentDate : PrefillDate()

  data class PrefillSpecificDate(val date: Instant) : PrefillDate()
}

data class CreateNewBloodSugarEntry(
    val patientUuid: UUID,
    val userEnteredDate: LocalDate,
    val prefilledDate: LocalDate,
    val bloodSugarReading: BloodSugarReading
) : BloodSugarEntryEffect() {
  val wasDateChanged: Boolean
    get() = userEnteredDate != prefilledDate
}

data class UpdateBloodSugarEntry(
    val bloodSugarMeasurementUuid: UUID,
    val userEnteredDate: LocalDate,
    val prefilledDate: LocalDate,
    val bloodSugarReading: BloodSugarReading
) : BloodSugarEntryEffect() {
  val wasDateChanged: Boolean
    get() = userEnteredDate != prefilledDate
}

data class FetchBloodSugarMeasurement(val bloodSugarMeasurementUuid: UUID) : BloodSugarEntryEffect()

object LoadBloodSugarUnitPreference : BloodSugarEntryEffect()

sealed class BloodSugarEntryViewEffect : BloodSugarEntryEffect()

data class SetBloodSugarReading(val bloodSugarReading: String) : BloodSugarEntryViewEffect()

object HideBloodSugarErrorMessage : BloodSugarEntryViewEffect()

object HideDateErrorMessage : BloodSugarEntryViewEffect()

object Dismiss : BloodSugarEntryViewEffect()

object ShowDateEntryScreen : BloodSugarEntryViewEffect()

object SetBloodSugarSavedResultAndFinish : BloodSugarEntryViewEffect()

data class ShowConfirmRemoveBloodSugarDialog(val bloodSugarMeasurementUuid: UUID) : BloodSugarEntryViewEffect()

data class ShowBloodSugarUnitSelectionDialog(val bloodSugarUnitPreference: BloodSugarUnitPreference) : BloodSugarEntryViewEffect()

data class ShowBloodSugarValidationError(
    val result: ValidationResult,
    val unitPreference: BloodSugarUnitPreference
) : BloodSugarEntryViewEffect()

data class ShowDateValidationError(val result: UserInputDateValidator.Result) : BloodSugarEntryViewEffect()

data class ShowBloodSugarEntryScreen(val date: LocalDate) : BloodSugarEntryViewEffect()

data class PrefillDates(val date: LocalDate) : BloodSugarEntryViewEffect()
