package org.simple.clinic.shortcodesearchresult

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.plumbing.AsyncOp
import org.simple.clinic.plumbing.AsyncOp.IN_FLIGHT
import org.simple.clinic.plumbing.AsyncOp.SUCCEEDED
import org.simple.clinic.searchresultsview.PatientSearchResults

@Parcelize
data class IdentifierSearchResultState(
    val shortCode: String,
    val fetchPatientsAsyncOp: AsyncOp,
    val patients: PatientSearchResults
): Parcelable {
  companion object {
    fun fetchingPatients(
        bpPassportNumber: String
    ): IdentifierSearchResultState =
        IdentifierSearchResultState(bpPassportNumber, IN_FLIGHT, PatientSearchResults.EMPTY_RESULTS)
  }

  fun patientsFetched(
      patientSearchResults: PatientSearchResults
  ): IdentifierSearchResultState =
      this.copy(fetchPatientsAsyncOp = SUCCEEDED, patients = patientSearchResults)

  fun noMatchingPatients(): IdentifierSearchResultState =
      this.copy(fetchPatientsAsyncOp = SUCCEEDED, patients = PatientSearchResults.EMPTY_RESULTS)
}
