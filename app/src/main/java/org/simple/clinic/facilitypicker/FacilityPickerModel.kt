package org.simple.clinic.facilitypicker

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.location.LocationUpdate

@Parcelize
data class FacilityPickerModel(
    val searchQuery: String,
    val facilities: List<Facility>?,
    val currentLocation: LocationUpdate?,
    val totalFacilityCount: Int?
) : Parcelable {

  companion object {
    fun create(): FacilityPickerModel {
      return FacilityPickerModel(
          searchQuery = "",
          facilities = null,
          currentLocation = null,
          totalFacilityCount = null
      )
    }
  }

  val hasLoadedFacilities: Boolean
    get() = facilities != null && currentLocation != null

  val hasFetchedLocation: Boolean
    get() = currentLocation != null

  val hasLoadedTotalFacilityCount: Boolean
    get() = totalFacilityCount != null

  fun queryChanged(query: String): FacilityPickerModel {
    return copy(searchQuery = query)
  }

  fun facilitiesLoaded(facilities: List<Facility>): FacilityPickerModel {
    return copy(facilities = facilities)
  }

  fun locationFetched(locationUpdate: LocationUpdate): FacilityPickerModel {
    return copy(currentLocation = locationUpdate)
  }

  fun facilityCountLoaded(count: Int): FacilityPickerModel {
    return copy(totalFacilityCount = count)
  }
}
