package org.simple.clinic.search.results

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class PatientSearchResultsModel : Parcelable {

  companion object {
    fun create(): PatientSearchResultsModel = PatientSearchResultsModel()
  }
}
