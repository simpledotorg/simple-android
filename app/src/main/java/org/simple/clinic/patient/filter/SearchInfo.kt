package org.simple.clinic.patient.filter

import org.simple.clinic.patient.PatientSearchResult

data class SearchInfo(
    val patient: PatientSearchResult.PatientNameAndId,
    val nameParts: List<String>,
    val searchParts: List<String>,
    val distances: List<Double> = emptyList()
)
