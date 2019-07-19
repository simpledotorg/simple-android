package org.simple.clinic.searchresultsview

import org.simple.clinic.patient.PatientSearchResult

data class PatientSearchResults(
    val visitedCurrentFacility: List<PatientSearchResult>,
    val notVisitedCurrentFacility: List<PatientSearchResult>
)
