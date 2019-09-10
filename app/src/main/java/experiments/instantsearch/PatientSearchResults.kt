package experiments.instantsearch

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult

data class PatientSearchResults(
    val visitedCurrentFacility: List<PatientSearchResult>,
    val notVisitedCurrentFacility: List<PatientSearchResult>,
    val currentFacility: Facility?,
    val searchQuery: String
) {
  companion object {
    val EMPTY_RESULTS = PatientSearchResults(emptyList(), emptyList(), null, "")
  }

  val hasNoResults: Boolean
    get() = visitedCurrentFacility.isEmpty() && notVisitedCurrentFacility.isEmpty()

  fun withSearchQuery(searchQuery: String): PatientSearchResults = this.copy(searchQuery = searchQuery)
}
