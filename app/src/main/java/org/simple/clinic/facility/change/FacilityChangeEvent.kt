package org.simple.clinic.facility.change

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

sealed class FacilityChangeEvent: UiEvent

data class CurrentFacilityLoaded(val facility: Facility): FacilityChangeEvent()
