package org.simple.clinic.bp.entry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.BP_ENTRY
import org.threeten.bp.LocalDate

@Parcelize
data class BloodPressureEntryModel(
    val openAs: OpenAs,
    val year: String,
    val systolic: String = "",
    val diastolic: String = "",
    val activeScreen: ScreenType = BP_ENTRY,
    val day: String = "",
    val month: String = "",
    val twoDigitYear: String = "",
    val prefilledDate: LocalDate? = null
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
    copy(diastolic = diastolic.substring(0, diastolic.length - 1))
  else
    this

  fun deleteSystolicLastDigit(): BloodPressureEntryModel = if (systolic.isNotEmpty())
    copy(systolic = systolic.substring(0, systolic.length - 1))
  else
    this

  fun screenChanged(activeScreen: ScreenType): BloodPressureEntryModel =
      copy(activeScreen = activeScreen)

  fun dayChanged(day: String): BloodPressureEntryModel =
      copy(day = day)

  fun monthChanged(month: String): BloodPressureEntryModel =
      copy(month = month)

  fun yearChanged(twoDigitYear: String): BloodPressureEntryModel =
      copy(twoDigitYear = twoDigitYear)

  fun datePrefilled(prefilledDate: LocalDate): BloodPressureEntryModel =
      copy(prefilledDate = prefilledDate)
}
