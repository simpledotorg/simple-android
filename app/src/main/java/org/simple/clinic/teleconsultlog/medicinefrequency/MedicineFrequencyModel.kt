package org.simple.clinic.teleconsultlog.medicinefrequency

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MedicineFrequencyModel(
    val medicineFrequency: MedicineFrequency
) : Parcelable {

  companion object {
    fun create(medicineFrequency: MedicineFrequency) = MedicineFrequencyModel(
        medicineFrequency = medicineFrequency
    )
  }

  fun medicineFrequencyChanged(medicineFrequency: MedicineFrequency): MedicineFrequencyModel {
    return copy(medicineFrequency = medicineFrequency)
  }

}
