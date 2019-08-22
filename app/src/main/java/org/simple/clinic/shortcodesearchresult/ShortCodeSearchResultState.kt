package org.simple.clinic.shortcodesearchresult

import org.simple.clinic.allpatientsinfacility.PatientSearchResultUiState
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.plumbing.AsyncOp
import org.simple.clinic.plumbing.AsyncOp.IN_FLIGHT
import org.simple.clinic.plumbing.AsyncOp.SUCCEEDED
import java.util.Collections.emptyList
import javax.inject.Inject

data class ShortCodeSearchResultState(
    val shortCode: String,
    val fetchPatientsAsyncOp: AsyncOp,
    val patients: List<PatientSearchResultUiState>
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
      this.copy(fetchPatientsAsyncOp = SUCCEEDED, patients = patientSearchResults.map(::PatientSearchResultUiState))

  fun noMatchingPatients(): ShortCodeSearchResultState =
      this.copy(fetchPatientsAsyncOp = SUCCEEDED, patients = emptyList())
}
