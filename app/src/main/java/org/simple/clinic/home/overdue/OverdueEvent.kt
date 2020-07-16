package org.simple.clinic.home.overdue

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

sealed class OverdueEvent: UiEvent

data class CurrentFacilityLoaded(val facility: Facility): OverdueEvent()
