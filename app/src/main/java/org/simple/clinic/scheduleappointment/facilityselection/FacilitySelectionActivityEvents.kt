package org.simple.clinic.scheduleappointment.facilityselection

import org.simple.clinic.facility.Facility
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent

data class FacilitySelected(val facility: Facility) : UiEvent {
  override val analyticsName = "Patient Facility Selection:Facility Clicked"
}

data class FacilitySelectionLocationPermissionChanged(val result: RuntimePermissionResult) : UiEvent {
  override val analyticsName = "Patient Facility Selection:Location Permission Changed to $result"
}

data class FacilitySelectionSearchQueryChanged(val query: String) : UiEvent {
  override val analyticsName = "Patient Facility Selection:Facility Search Query Changed"
}

data class FacilitySelectionUserLocationUpdated(val location: LocationUpdate) : UiEvent {
  override val analyticsName = "Patient Facility Selection:Location Updated ${when (location) {
    is LocationUpdate.Unavailable -> "TurnedOff"
    is LocationUpdate.Available -> "Available"
  }}"
}

