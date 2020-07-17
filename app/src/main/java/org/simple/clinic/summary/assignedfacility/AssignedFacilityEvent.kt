package org.simple.clinic.summary.assignedfacility

import org.simple.clinic.facility.Facility
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent

sealed class AssignedFacilityEvent : UiEvent

data class AssignedFacilityLoaded(val facility: Optional<Facility>) : AssignedFacilityEvent()

object FacilityChanged : AssignedFacilityEvent()
