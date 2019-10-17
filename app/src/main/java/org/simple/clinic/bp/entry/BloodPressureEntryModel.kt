package org.simple.clinic.bp.entry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.BP_ENTRY
import org.simple.clinic.bp.entry.OpenAs.New
import org.simple.clinic.bp.entry.OpenAs.Update

@Parcelize
data class BloodPressureEntryModel(
    val openAs: OpenAs,
    val systolic: String = "",
    val diastolic: String = "",
    val activeScreen: ScreenType = BP_ENTRY
) : Parcelable {
  companion object {
    fun newBloodPressureEntry(openAsNew: New): BloodPressureEntryModel =
        BloodPressureEntryModel(openAsNew)

    fun updateBloodPressureEntry(openAsUpdate: Update): BloodPressureEntryModel =
        BloodPressureEntryModel(openAsUpdate)
  }

  // TODO(rj) 17/Oct/19 - Rename to `systolicChanged`
  fun withSystolic(systolic: String): BloodPressureEntryModel =
      copy(systolic = systolic)

  // TODO(rj) 17/Oct/19 - Rename to `diastolicChanged`
  fun withDiastolic(diastolic: String): BloodPressureEntryModel =
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
}
