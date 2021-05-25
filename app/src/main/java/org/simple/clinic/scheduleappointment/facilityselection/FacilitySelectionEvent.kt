package org.simple.clinic.scheduleappointment.facilityselection

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

sealed class FacilitySelectionEvent : UiEvent

data class FacilitySelected(val facility: Facility) : FacilitySelectionEvent() {
  override val analyticsName = "Patient Facility Selection:Facility Clicked"
}
