package org.simple.clinic.allpatientsinfacility

import org.simple.clinic.facility.Facility

sealed class AllPatientsInFacilityEvent

data class FacilityFetchedEvent(val facility: Facility) : AllPatientsInFacilityEvent()

object NoPatientsInFacilityEvent : AllPatientsInFacilityEvent()

data class HasPatientsInFacilityEvent(val patients: List<PatientSearchResultUiState>) : AllPatientsInFacilityEvent()
