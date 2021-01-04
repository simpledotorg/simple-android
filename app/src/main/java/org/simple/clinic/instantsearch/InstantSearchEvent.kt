package org.simple.clinic.instantsearch

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class InstantSearchEvent : UiEvent

data class CurrentFacilityLoaded(val facility: Facility) : InstantSearchEvent()

data class AllPatientsLoaded(val patients: List<PatientSearchResult>) : InstantSearchEvent()

data class SearchResultsLoaded(val patientsSearchResults: List<PatientSearchResult>) : InstantSearchEvent()

data class SearchQueryValidated(val result: InstantSearchValidator.Result) : InstantSearchEvent()

data class SearchResultClicked(val patientId: UUID) : InstantSearchEvent() {

  override val analyticsName: String = "Instant Search: Search Result Clicked"
}

data class SearchQueryChanged(val searchQuery: String) : InstantSearchEvent()

object SavedNewOngoingPatientEntry : InstantSearchEvent()

object RegisterNewPatientClicked : InstantSearchEvent() {

  override val analyticsName: String = "Instant Search: Register New Patient"
}
