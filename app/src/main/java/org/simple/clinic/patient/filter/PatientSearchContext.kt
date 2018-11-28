package org.simple.clinic.patient.filter

import org.simple.clinic.patient.PatientSearchResult

data class PatientSearchContext(
    val patient: PatientSearchResult.PatientNameAndId,
    val nameParts: List<String>,
    val searchParts: List<String>,
    val editDistances: List<EditDistance> = emptyList()
) {
  val totalEditDistance: Double
    get() = editDistances.map { it.editDistance }.sum()

  data class EditDistance(val namePart: String, val searchPart: String, val editDistance: Double)
}
