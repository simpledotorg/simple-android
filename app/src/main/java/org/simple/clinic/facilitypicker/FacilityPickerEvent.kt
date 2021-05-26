package org.simple.clinic.facilitypicker

import org.simple.clinic.facility.Facility
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.widgets.UiEvent

sealed class FacilityPickerEvent : UiEvent

data class LocationFetched(val update: LocationUpdate) : FacilityPickerEvent()

data class FacilitiesFetched(
    val query: String,
    val facilities: List<Facility>
) : FacilityPickerEvent()

data class SearchQueryChanged(val query: String) : FacilityPickerEvent() {
  override val analyticsName = "Facility Picker:Search Query Changed"
}

data class TotalFacilityCountLoaded(val count: Int) : FacilityPickerEvent()

data class FacilityClicked(val facility: Facility) : FacilityPickerEvent() {
  override val analyticsName = "Facility Picker:Done Clicked"
}
