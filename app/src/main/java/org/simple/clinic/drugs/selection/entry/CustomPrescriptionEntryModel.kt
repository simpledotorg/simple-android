package org.simple.clinic.drugs.selection.entry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CustomPrescriptionEntryModel(val drugName: String?, val dosage: String?) : Parcelable {

  companion object {
    fun create(): CustomPrescriptionEntryModel {
      return CustomPrescriptionEntryModel(null, null)
    }
  }

  fun drugNameChanged(name: String): CustomPrescriptionEntryModel {
    return copy(drugName = name)
  }

  fun dosageChanged(dosage: String): CustomPrescriptionEntryModel {
    return copy(dosage = dosage)
  }
}
