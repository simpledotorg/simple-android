package org.simple.clinic.searchresultsview

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult

@Parcelize
data class PatientSearchResults(
    val visitedCurrentFacility: List<PatientSearchResult>,
    val notVisitedCurrentFacility: List<PatientSearchResult>,
    val currentFacility: Facility?
): Parcelable {
  companion object {
    val EMPTY_RESULTS = PatientSearchResults(emptyList(), emptyList(), null)
  }

  val hasNoResults: Boolean
    get() = visitedCurrentFacility.isEmpty() && notVisitedCurrentFacility.isEmpty()

}
