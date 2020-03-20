package org.simple.clinic.facility.change

import org.simple.clinic.facility.Facility
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent

data class FacilityChangeClicked(val facility: Facility) : UiEvent {
  override val analyticsName = "Facility Change:Facility Clicked"
}

data class FacilityChangeLocationPermissionChanged(val result: RuntimePermissionResult) : UiEvent {
  override val analyticsName = "Facility Change:Location Permission Changed to $result"
}

data class FacilityChangeSearchQueryChanged(val query: String): UiEvent {
  override val analyticsName = "Facility Change:Facility Search Query Changed"
}

data class FacilityChangeUserLocationUpdated(val location: LocationUpdate) : UiEvent {
  override val analyticsName = "Facility Change:Location Updated ${when (location) {
    is LocationUpdate.Unavailable -> "TurnedOff"
    is LocationUpdate.Available -> "Available"
  }}"
}
