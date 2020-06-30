package org.simple.clinic.registration.facility

import org.simple.clinic.facility.Facility
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.widgets.UiEvent

sealed class RegistrationFacilitySelectionEvent : UiEvent

data class LocationFetched(val update: LocationUpdate) : RegistrationFacilitySelectionEvent()

data class FacilitiesFetched(val query: String, val facilities: List<Facility>) : RegistrationFacilitySelectionEvent()

data class RegistrationFacilitySearchQueryChanged(val query: String) : RegistrationFacilitySelectionEvent() {
  override val analyticsName = "Registration:Facility Selection:Search Query Changed"
}

data class TotalFacilityCountLoaded(val count: Int) : RegistrationFacilitySelectionEvent()

data class RegistrationFacilityClicked(val facility: Facility) : RegistrationFacilitySelectionEvent() {
  override val analyticsName = "Registration:Facility Selection:Done Clicked"
}

object CurrentRegistrationEntrySaved: RegistrationFacilitySelectionEvent()
