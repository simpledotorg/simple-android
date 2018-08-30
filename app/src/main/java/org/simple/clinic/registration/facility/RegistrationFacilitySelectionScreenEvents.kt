package org.simple.clinic.registration.facility

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

class RegistrationFacilitySelectionRetryClicked : UiEvent {
  override val analyticsName = "Registration:Facility Selection:Retry Clicked"
}

data class RegistrationFacilityClicked(val facility: Facility) : UiEvent {
  override val analyticsName = "Registration:Facility Selection:Done Clicked"
}
