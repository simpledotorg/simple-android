package org.simple.clinic.scheduleappointment.facilityselection

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

data class FacilitySelected(val facility: Facility) : UiEvent {
  override val analyticsName = "Patient Facility Selection:Facility Clicked"
}

