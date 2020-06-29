package org.simple.clinic.registration.facility

import org.simple.clinic.facility.Facility
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class RegistrationFacilityClicked(val facility: Facility) : UiEvent {
  override val analyticsName = "Registration:Facility Selection:Done Clicked"
}

data class RegistrationFacilityUserLocationUpdated(val location: LocationUpdate) : UiEvent {
  override val analyticsName = "Registration:Facility Selection:Location Updated ${when (location) {
    is LocationUpdate.Unavailable -> "TurnedOff"
    is LocationUpdate.Available -> "Available"
  }}"
}

data class RegistrationFacilityConfirmed(val facilityUuid: UUID) : UiEvent {
  override val analyticsName: String = "Registration:Facility Selection:Confirmed Facility Selection"
}
