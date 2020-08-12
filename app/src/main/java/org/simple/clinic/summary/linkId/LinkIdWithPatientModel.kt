package org.simple.clinic.summary.linkId

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

@Parcelize
data class LinkIdWithPatientModel(
    val patientUuid: UUID?,
    val identifier: Identifier?
) : Parcelable {

  companion object {

    fun create() = LinkIdWithPatientModel(
        patientUuid = null,
        identifier = null
    )
  }

  val hasIdentifier: Boolean
    get() = identifier != null

  fun linkIdWithPatientViewShown(patientUuid: UUID, identifier: Identifier): LinkIdWithPatientModel {
    return copy(patientUuid = patientUuid, identifier = identifier)
  }
}
