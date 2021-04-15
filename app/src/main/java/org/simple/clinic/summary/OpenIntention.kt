package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

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

  @Parcelize
  data class ViewExistingPatientWithTeleconsultLog(val teleconsultRecordId: UUID) : OpenIntention() {
    override fun analyticsName() = "DEEP_LINK_TELECONSULT_LOG"
  }
}
