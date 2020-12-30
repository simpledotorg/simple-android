package org.simple.clinic.instantsearch

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult
import java.util.UUID

sealed class InstantSearchEvent

data class CurrentFacilityLoaded(val facility: Facility) : InstantSearchEvent()

data class AllPatientsLoaded(val patients: List<PatientSearchResult>) : InstantSearchEvent()

data class SearchResultsLoaded(val patientsSearchResults: List<PatientSearchResult>) : InstantSearchEvent()

data class SearchQueryValidated(val result: InstantSearchValidator.Result) : InstantSearchEvent()

data class SearchResultClicked(val patientId: UUID) : InstantSearchEvent()
