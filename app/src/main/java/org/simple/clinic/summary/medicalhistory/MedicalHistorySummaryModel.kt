package org.simple.clinic.summary.medicalhistory

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class MedicalHistorySummaryModel: Parcelable {

  companion object {
    fun create(): MedicalHistorySummaryModel = MedicalHistorySummaryModel()
  }
}
