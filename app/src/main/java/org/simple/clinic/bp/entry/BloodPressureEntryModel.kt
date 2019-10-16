package org.simple.clinic.bp.entry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.bp.entry.OpenAs.New
import org.simple.clinic.bp.entry.OpenAs.Update

@Parcelize
data class BloodPressureEntryModel(
    val openAs: OpenAs
) : Parcelable {
  companion object {
    fun newBloodPressureEntry(openAsNew: New): BloodPressureEntryModel =
        BloodPressureEntryModel(openAsNew)

    fun updateBloodPressureEntry(openAsUpdate: Update): BloodPressureEntryModel =
        BloodPressureEntryModel(openAsUpdate)
  }
}
