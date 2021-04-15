package org.simple.clinic.facility.change

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility

@Parcelize
data class FacilityChangeModel(
    val currentFacility: Facility?
) : Parcelable {

  companion object {
    fun create(): FacilityChangeModel {
      return FacilityChangeModel(currentFacility = null)
    }
  }

  val hasLoadedCurrentFacility: Boolean
    get() = currentFacility != null

  fun currentFacilityLoaded(facility: Facility): FacilityChangeModel {
    return copy(currentFacility = facility)
  }
}
