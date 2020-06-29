package org.simple.clinic.registration.facility

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.facility.Facility

@Parcelize
data class RegistrationFacilitySelectionModel(
    val searchQuery: String,
    val facilities: List<Facility>?
) : Parcelable {

  companion object {
    fun create(): RegistrationFacilitySelectionModel = RegistrationFacilitySelectionModel(
        searchQuery = "",
        facilities = null
    )
  }

  fun queryChanged(query: String): RegistrationFacilitySelectionModel {
    return copy(searchQuery = query)
  }

  fun facilitiesLoaded(facilities: List<Facility>): RegistrationFacilitySelectionModel {
    return copy(facilities = facilities)
  }
}
