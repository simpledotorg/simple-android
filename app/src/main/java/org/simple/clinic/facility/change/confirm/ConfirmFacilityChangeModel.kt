package org.simple.clinic.facility.change.confirm

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.facility.Facility

@Parcelize
data class ConfirmFacilityChangeModel(
    val currentFacility: Facility?
) : Parcelable {

  companion object {
    fun create(): ConfirmFacilityChangeModel = ConfirmFacilityChangeModel(
        currentFacility = null
    )
  }
}
