package org.simple.clinic.patient.filter

import org.simple.clinic.patient.PatientSearchResult

data class EditDistanceRecord(val namePart: String, val searchPart: String, val editDistance: Double)

data class SearchContext(
    val patient: PatientSearchResult.PatientNameAndId,
    val nameParts: List<String>,
    val searchParts: List<String>,
    val editDistanceRecords: List<EditDistanceRecord> = emptyList()
) {
  val totalEditDistance: Double
    get() = editDistanceRecords.map { it.editDistance }.sum()
}
