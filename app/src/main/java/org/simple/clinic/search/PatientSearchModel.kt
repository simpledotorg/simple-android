package org.simple.clinic.search

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PatientSearchModel(
    private val enteredQuery: String
) : Parcelable {

  companion object {
    fun create(): PatientSearchModel = PatientSearchModel(
        enteredQuery = ""
    )
  }

  fun queryChanged(query: String): PatientSearchModel {
    return copy(enteredQuery = query)
  }
}
