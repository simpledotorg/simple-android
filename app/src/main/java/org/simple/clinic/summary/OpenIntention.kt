package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.businessid.Identifier

sealed class OpenIntention : Parcelable {

  abstract fun analyticsName(): String

  @Parcelize
  object ViewExistingPatient : OpenIntention() {
    override fun analyticsName() = "SEARCH"
  }

  @Parcelize
  object ViewNewPatient : OpenIntention() {
    override fun analyticsName() = "NEW_PATIENT"
  }

  @Parcelize
  data class LinkIdWithPatient(val identifier: Identifier) : OpenIntention() {
    override fun analyticsName() = "LINK_ID_WITH_PATIENT"
  }
}
