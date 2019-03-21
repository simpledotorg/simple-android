package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.addidtopatient.AddIdToPatient

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

  @Parcelize
  data class LinkIdWithPatient(val addIdToPatient: AddIdToPatient) : PatientSummaryCaller() {
    override fun analyticsName() = "LINK_ID_WITH_PATIENT"
  }
}
