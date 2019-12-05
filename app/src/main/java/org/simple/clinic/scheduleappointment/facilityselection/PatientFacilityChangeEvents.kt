package org.simple.clinic.scheduleappointment.patientFacilityTransfer

import org.simple.clinic.facility.Facility
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent

data class PatientFacilityChangeClicked(val facility: Facility) : UiEvent {
  override val analyticsName = "Facility Change:Facility Clicked"
}

data class PatientFacilityLocationPermissionChanged(val result: RuntimePermissionResult) : UiEvent {
  override val analyticsName = "Facility Change:Location Permission Changed to $result"
}

data class PatientFacilitySearchQueryChanged(val query: String) : UiEvent {
  override val analyticsName = "Facility Change:Facility Search Query Changed"
}

data class PatientFacilityUserLocationUpdated(val location: LocationUpdate) : UiEvent {
  override val analyticsName = "Facility Change:Location Updated ${when (location) {
    is LocationUpdate.Unavailable -> "TurnedOff"
    is LocationUpdate.Available -> "Available"
  }}"
}

