package org.simple.clinic.registration.facility

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

class RegistrationFacilitySelectionRetryClicked : UiEvent {
  override val analyticsName = "Registration:Facility Selection:Retry Clicked"
}

data class RegistrationFacilitySelectionChanged(val facility: Facility, val isSelected: Boolean) : UiEvent {
  override val analyticsName = "Registration:Facility Selection:Changed Selection Of A Facility"
}

data class RegistrationSelectedFacilitiesChanged(val selectedFacilities: Set<Facility>) : UiEvent {
  override val analyticsName = "Registration:Facility Selection:Changed Selected Facilities"
}

class RegistrationFacilitySelectionDoneClicked : UiEvent {
  override val analyticsName = "Registration:Facility Selection:Done Clicked"
}
