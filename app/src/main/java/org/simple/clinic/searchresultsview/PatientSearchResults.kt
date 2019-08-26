package org.simple.clinic.searchresultsview

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult

data class PatientSearchResults(
    val visitedCurrentFacility: List<PatientSearchResult>,
    val notVisitedCurrentFacility: List<PatientSearchResult>,
    val currentFacility: Facility?
) {
  companion object {
    fun emptyResults() = PatientSearchResults(emptyList(), emptyList(), null)
  }

  val hasNoResults = visitedCurrentFacility.isEmpty() && notVisitedCurrentFacility.isEmpty()
}
