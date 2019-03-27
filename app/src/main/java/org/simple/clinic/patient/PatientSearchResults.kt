package org.simple.clinic.patient

data class PatientSearchResults(
    val visitedCurrentFacility: List<PatientSearchResult>,
    val notVisitedCurrentFacility: List<PatientSearchResult>
)

fun PatientSearchResults.allPatientSearchResults() = visitedCurrentFacility + notVisitedCurrentFacility
