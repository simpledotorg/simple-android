package org.simple.clinic.shortcodesearchresult

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.plumbing.AsyncOp
import org.simple.clinic.plumbing.AsyncOp.IN_FLIGHT
import org.simple.clinic.plumbing.AsyncOp.SUCCEEDED
import org.simple.clinic.searchresultsview.PatientSearchResults

@Parcelize
data class ShortCodeSearchResultState(
    val shortCode: String,
    val fetchPatientsAsyncOp: AsyncOp,
    val patients: PatientSearchResults
): Parcelable {
  companion object {
    fun fetchingPatients(
        bpPassportNumber: String
    ): ShortCodeSearchResultState =
        ShortCodeSearchResultState(bpPassportNumber, IN_FLIGHT, PatientSearchResults.EMPTY_RESULTS)
  }

  fun patientsFetched(
      patientSearchResults: PatientSearchResults
  ): ShortCodeSearchResultState =
      this.copy(fetchPatientsAsyncOp = SUCCEEDED, patients = patientSearchResults)

  fun noMatchingPatients(): ShortCodeSearchResultState =
      this.copy(fetchPatientsAsyncOp = SUCCEEDED, patients = PatientSearchResults.EMPTY_RESULTS)
}
