package org.simple.clinic.registration.facility

import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.widgets.UiEvent

data class RegistrationFacilityUserLocationUpdated(val location: LocationUpdate) : UiEvent {
  override val analyticsName = "Registration:Facility Selection:Location Updated ${when (location) {
    is LocationUpdate.Unavailable -> "TurnedOff"
    is LocationUpdate.Available -> "Available"
  }}"
}
