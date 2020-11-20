package org.simple.clinic.registration.facility

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class RegistrationFacilitySelectionEvent : UiEvent

data class RegistrationFacilityClicked(val facility: Facility) : RegistrationFacilitySelectionEvent() {
  override val analyticsName = "Registration:Facility Selection:Done Clicked"
}

data class RegistrationFacilityConfirmed(val facilityUuid: UUID) : RegistrationFacilitySelectionEvent() {
  override val analyticsName: String = "Registration:Facility Selection:Confirmed Facility Selection"
}
