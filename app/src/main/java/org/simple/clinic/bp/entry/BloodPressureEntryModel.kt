package org.simple.clinic.bp.entry

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.BP_ENTRY
import org.simple.clinic.bp.entry.BloodPressureSaveState.NOT_SAVING_BLOOD_PRESSURE
import java.time.LocalDate

@Parcelize
data class BloodPressureEntryModel(
    val openAs: OpenAs,
    val year: String,
    val systolic: String = "",
    val diastolic: String = "",
    val activeScreen: ScreenType = BP_ENTRY,
    val day: String = "",
    val month: String = "",
    val fourDigitYear: String = "",
    val prefilledDate: LocalDate? = null,
    val bloodPressureSaveState: BloodPressureSaveState = NOT_SAVING_BLOOD_PRESSURE
) : Parcelable {
  companion object {
    fun create(openAs: OpenAs, year: Int): BloodPressureEntryModel =
        BloodPressureEntryModel(openAs, year.toString())
  }

  fun systolicChanged(systolic: String): BloodPressureEntryModel =
      copy(systolic = systolic)

  fun diastolicChanged(diastolic: String): BloodPressureEntryModel =
      copy(diastolic = diastolic)

  fun deleteDiastolicLastDigit(): BloodPressureEntryModel = if (diastolic.isNotEmpty())
    copy(diastolic = diastolic.unsafeDropLastChar())
  else
    this

  fun deleteSystolicLastDigit(): BloodPressureEntryModel = if (systolic.isNotEmpty())
    copy(systolic = systolic.unsafeDropLastChar())
  else
    this

  fun screenChanged(activeScreen: ScreenType): BloodPressureEntryModel =
      copy(activeScreen = activeScreen)

  fun dayChanged(day: String): BloodPressureEntryModel =
      copy(day = day)

  fun monthChanged(month: String): BloodPressureEntryModel =
      copy(month = month)

  fun yearChanged(fourDigitYear: String): BloodPressureEntryModel =
      copy(fourDigitYear = fourDigitYear)

  fun datePrefilled(prefilledDate: LocalDate): BloodPressureEntryModel =
      copy(prefilledDate = prefilledDate)

  fun bloodPressureStateChanged(bloodPressureSaveState: BloodPressureSaveState): BloodPressureEntryModel =
      copy(bloodPressureSaveState = bloodPressureSaveState)

  private fun String.unsafeDropLastChar(): String =
      this.substring(0, this.length - 1)
}
