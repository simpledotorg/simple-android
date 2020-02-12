package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.LocalDate
import java.util.UUID

sealed class BloodSugarEntryEffect

sealed class PrefillDate : BloodSugarEntryEffect() {
  companion object {
    fun forNewEntry(): PrefillDate {
      return PrefillCurrentDate
    }
  }

  object PrefillCurrentDate : PrefillDate()
}

object HideBloodSugarErrorMessage : BloodSugarEntryEffect()

object HideDateErrorMessage : BloodSugarEntryEffect()

object Dismiss : BloodSugarEntryEffect()

object ShowDateEntryScreen : BloodSugarEntryEffect()

data class ShowBloodSugarValidationError(val result: BloodSugarValidator.Result) : BloodSugarEntryEffect()

data class ShowBloodSugarEntryScreen(val date: LocalDate) : BloodSugarEntryEffect()

data class ShowDateValidationError(val result: UserInputDateValidator.Result) : BloodSugarEntryEffect()

data class CreateNewBloodSugarEntry(
    val patientUuid: UUID,
    val bloodSugarReading: Int,
    val measurementType: BloodSugarMeasurementType,
    val userEnteredDate: LocalDate,
    val prefilledDate: LocalDate
) : BloodSugarEntryEffect() {
  val wasDateChanged: Boolean
    get() = userEnteredDate != prefilledDate
}

object SetBloodSugarSavedResultAndFinish : BloodSugarEntryEffect()

data class FetchBloodSugarMeasurement(val bloodSugarMeasurementUuid: UUID) : BloodSugarEntryEffect()
