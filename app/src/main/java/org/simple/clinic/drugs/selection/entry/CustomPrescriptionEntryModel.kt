package org.simple.clinic.drugs.selection.entry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CustomPrescriptionEntryModel(val drugName: String?) : Parcelable {

  companion object {
    fun create() : CustomPrescriptionEntryModel {
      return CustomPrescriptionEntryModel(null)
    }
  }
}
