package org.simple.clinic.medicalhistory.newentry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry

@Parcelize
data class NewMedicalHistoryModel(val ongoingMedicalHistoryEntry: OngoingMedicalHistoryEntry): Parcelable {

  companion object {
    fun default(): NewMedicalHistoryModel = NewMedicalHistoryModel(OngoingMedicalHistoryEntry())
  }
}
