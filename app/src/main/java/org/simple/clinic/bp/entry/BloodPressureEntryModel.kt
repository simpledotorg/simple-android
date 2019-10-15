package org.simple.clinic.bp.entry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.bp.entry.OpenAs.New
import org.simple.clinic.bp.entry.OpenAs.Update
import java.util.UUID

@Parcelize
data class BloodPressureEntryModel(
    val openAs: OpenAs
) : Parcelable {
  companion object {
    fun newBloodPressureEntry(patientUuid: UUID): BloodPressureEntryModel =
        BloodPressureEntryModel(New(patientUuid))

    fun updateBloodPressureEntry(bpUuid: UUID): BloodPressureEntryModel =
        BloodPressureEntryModel(Update(bpUuid))
  }
}
