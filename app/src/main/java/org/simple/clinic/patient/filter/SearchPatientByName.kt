package org.simple.clinic.patient.filter

import io.reactivex.Single
import org.simple.clinic.patient.PatientSearchResult
import java.util.UUID

interface SearchPatientByName {
  fun search(searchTerm: String, names: List<PatientSearchResult.PatientNameAndId>): Single<List<UUID>>
}
