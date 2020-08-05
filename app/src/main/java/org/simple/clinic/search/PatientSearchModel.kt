package org.simple.clinic.search

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class PatientSearchModel : Parcelable {

  companion object {
    fun create(): PatientSearchModel = PatientSearchModel()
  }
}
