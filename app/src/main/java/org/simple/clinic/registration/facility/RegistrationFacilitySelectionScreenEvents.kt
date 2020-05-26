package org.simple.clinic.registration.facility

import org.simple.clinic.facility.Facility
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class RegistrationFacilitySelectionRetryClicked : UiEvent {
  override val analyticsName = "Registration:Facility Selection:Retry Clicked"
}

data class RegistrationFacilityClicked(val facility: Facility) : UiEvent {
  override val analyticsName = "Registration:Facility Selection:Done Clicked"
}

data class RegistrationFacilitySearchQueryChanged(val query: String) : UiEvent {
  override val analyticsName = "Registration:Facility Selection:Search Query Changed"
}

data class RegistrationFacilityUserLocationUpdated(val location: LocationUpdate) : UiEvent {
  override val analyticsName = "Registration:Facility Selection:Location Updated ${when (location) {
    is LocationUpdate.Unavailable -> "TurnedOff"
    is LocationUpdate.Available -> "Available"
  }}"
}

data class RegistrationFacilityLocationPermissionChanged(val result: RuntimePermissionResult) : UiEvent {
  override val analyticsName = "Registration:Facility Selection:Location Permission Changed to $result"
}

data class RegistrationFacilityConfirmed(val facilityUuid: UUID) : UiEvent {
  override val analyticsName: String = "Registration:Facility Selection:Confirmed Facility Selection"
}
