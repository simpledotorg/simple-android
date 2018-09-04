package org.simple.clinic.facility.change

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

data class FacilityClicked(val facility: Facility) : UiEvent {
  override val analyticsName = "Facility Change:Facility Clicked"
}
