package org.simple.clinic.facilitypicker

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class FacilityPickerModel: Parcelable {

  companion object {
    fun create(): FacilityPickerModel {
      return FacilityPickerModel()
    }
  }
}
