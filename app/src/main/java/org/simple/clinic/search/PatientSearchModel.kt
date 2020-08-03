package org.simple.clinic.search

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PatientSearchModel(
    val enteredQuery: String,
    val validationErrors: Set<PatientSearchValidationError>
) : Parcelable {

  companion object {
    fun create(): PatientSearchModel = PatientSearchModel(
        enteredQuery = "",
        validationErrors = emptySet()
    )
  }

  fun queryChanged(query: String): PatientSearchModel {
    return copy(enteredQuery = query)
  }

  fun invalidQuery(validationErrors: Set<PatientSearchValidationError>): PatientSearchModel {
    return copy(validationErrors = validationErrors)
  }
}
