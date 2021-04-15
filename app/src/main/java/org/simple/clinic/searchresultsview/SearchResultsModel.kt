package org.simple.clinic.searchresultsview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchResultsModel(
    val patientSearchResults: PatientSearchResults?
) : Parcelable {

  companion object {
    fun create(): SearchResultsModel = SearchResultsModel(patientSearchResults = null)
  }

  val hasLoadedSearchResults: Boolean
    get() = patientSearchResults != null

  fun withSearchResults(patientSearchResults: PatientSearchResults): SearchResultsModel {
    return copy(patientSearchResults = patientSearchResults)
  }
}
