package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class PatientSummaryCaller : Parcelable {

  abstract fun analyticsName(): String

  @Parcelize
  object ExistingPatient : PatientSummaryCaller() {
    override fun analyticsName() = "SEARCH"
  }

  @Parcelize
  object NewPatient : PatientSummaryCaller() {
    override fun analyticsName() = "NEW_PATIENT"
  }
}
