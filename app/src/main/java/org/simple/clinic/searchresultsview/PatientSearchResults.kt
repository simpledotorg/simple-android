package org.simple.clinic.searchresultsview

import org.simple.clinic.patient.PatientSearchResult

data class PatientSearchResults(
    val visitedCurrentFacility: List<PatientSearchResult>,
    val notVisitedCurrentFacility: List<PatientSearchResult>
) {
  companion object {
    fun emptyResults() = PatientSearchResults(emptyList(), emptyList())
  }

  val hasNoResults = visitedCurrentFacility.isEmpty() && notVisitedCurrentFacility.isEmpty()
}
