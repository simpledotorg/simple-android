package org.simple.clinic.facility.change.confirm

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility

@Parcelize
data class ConfirmFacilityChangeModel(
    val currentFacility: Facility?
) : Parcelable {

  val hasLoadedCurrentFacility: Boolean
    get() = currentFacility != null

  companion object {
    fun create(): ConfirmFacilityChangeModel = ConfirmFacilityChangeModel(
        currentFacility = null
    )
  }

  fun hasFacilitySyncGroupSwitched(newFacility: Facility): Boolean {
    return currentFacility!!.syncGroup != newFacility.syncGroup
  }

  fun currentFacilityLoaded(facility: Facility): ConfirmFacilityChangeModel {
    return copy(currentFacility = facility)
  }
}
