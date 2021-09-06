package org.simple.clinic.summary.assignedfacility

import org.simple.clinic.facility.Facility
import java.util.Optional
import org.simple.clinic.widgets.UiEvent

sealed class AssignedFacilityEvent : UiEvent

data class AssignedFacilityLoaded(val facility: Optional<Facility>) : AssignedFacilityEvent() {
  override val analyticsName: String = "Assigned Facility:Facility Loaded"
}

data class AssignedFacilitySelected(val facility: Facility) : AssignedFacilityEvent() {
  override val analyticsName: String = "Assigned Facility:Facility Selected"
}
