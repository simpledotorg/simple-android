package org.simple.clinic.search.results

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.businessid.Identifier

@Parcelize
data class PatientSearchResultsModel(
    val searchCriteria: PatientSearchCriteria
) : Parcelable {

  companion object {
    fun create(searchCriteria: PatientSearchCriteria): PatientSearchResultsModel {
      return PatientSearchResultsModel(
          searchCriteria = searchCriteria
      )
    }
  }

  val hasAdditionalIdentifier: Boolean
    get() = additionalIdentifier != null

  val additionalIdentifier: Identifier?
    get() = searchCriteria.additionalIdentifier
}
