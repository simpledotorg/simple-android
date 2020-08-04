package org.simple.clinic.search.results

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.businessid.Identifier

@Parcelize
data class PatientSearchResultsModel(
    val additionalIdentifier: Identifier?
) : Parcelable {

  companion object {
    fun create(searchCriteria: PatientSearchCriteria): PatientSearchResultsModel {
      return PatientSearchResultsModel(
          additionalIdentifier = searchCriteria.additionalIdentifier
      )
    }
  }
}
