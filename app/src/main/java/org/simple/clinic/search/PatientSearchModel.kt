package org.simple.clinic.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.businessid.Identifier

@Parcelize
data class PatientSearchModel(
    val enteredQuery: String,
    val validationErrors: Set<PatientSearchValidationError>,
    val additionalIdentifier: Identifier?
) : Parcelable {

  companion object {
    fun create(
        additionalIdentifier: Identifier?
    ): PatientSearchModel = PatientSearchModel(
        enteredQuery = "",
        validationErrors = emptySet(),
        additionalIdentifier = additionalIdentifier
    )
  }

  fun queryChanged(query: String): PatientSearchModel {
    return copy(enteredQuery = query, validationErrors = emptySet())
  }

  fun invalidQuery(validationErrors: Set<PatientSearchValidationError>): PatientSearchModel {
    return copy(validationErrors = validationErrors)
  }
}
