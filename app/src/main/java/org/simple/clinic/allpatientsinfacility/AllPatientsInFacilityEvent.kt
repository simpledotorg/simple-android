package org.simple.clinic.allpatientsinfacility

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

sealed class AllPatientsInFacilityEvent : UiEvent

data class FacilityFetchedEvent(val facility: Facility) : AllPatientsInFacilityEvent()

object NoPatientsInFacilityEvent : AllPatientsInFacilityEvent()

data class HasPatientsInFacilityEvent(val patients: List<PatientSearchResultUiState>) : AllPatientsInFacilityEvent()
