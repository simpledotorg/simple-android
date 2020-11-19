package org.simple.clinic.bloodsugar.unitselection

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference

@Parcelize
data class BloodSugarUnitSelectionModel(
    val bloodSugarUnitPreference: BloodSugarUnitPreference
) : Parcelable {

  companion object {
    fun create(bloodSugarUnitPreference: BloodSugarUnitPreference): BloodSugarUnitSelectionModel {
      return BloodSugarUnitSelectionModel(
          bloodSugarUnitPreference = bloodSugarUnitPreference
      )
    }
  }

  fun bloodSugarUnitPreferenceChanged(bloodSugarUnitPreference: BloodSugarUnitPreference): BloodSugarUnitSelectionModel {
    return copy(bloodSugarUnitPreference = bloodSugarUnitPreference)
  }
}
