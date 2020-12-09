package org.simple.clinic.patient.filter

import org.simple.clinic.patient.PatientSearchResult
import java.util.UUID

interface SearchPatientByName {
  fun search(searchTerm: String, names: List<PatientSearchResult.PatientNameAndId>): List<UUID>
}
