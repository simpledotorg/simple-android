package org.simple.clinic.shortcodesearchresult

import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.plumbing.AsyncOp
import org.simple.clinic.plumbing.AsyncOp.IN_FLIGHT
import org.simple.clinic.plumbing.AsyncOp.SUCCEEDED

data class ShortCodeSearchResultState(
    val bpPassportNumber: String,
    val fetchPatientsAsyncOp: AsyncOp,
    val patients: List<PatientSearchResult>
) {
  companion object {
    fun fetchingPatients(
        bpPassportNumber: String
    ): ShortCodeSearchResultState =
        ShortCodeSearchResultState(bpPassportNumber, IN_FLIGHT, emptyList())
  }

  fun patientsFetched(
      patientSearchResults: List<PatientSearchResult>
  ): ShortCodeSearchResultState =
      this.copy(fetchPatientsAsyncOp = SUCCEEDED, patients = patientSearchResults)
}
