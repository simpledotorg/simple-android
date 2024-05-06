package org.simple.clinic.reassignPatient

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent
import java.util.Optional

sealed class ReassignPatientEvent : UiEvent

data class AssignedFacilityLoaded(val facility: Optional<Facility>) : ReassignPatientEvent()
