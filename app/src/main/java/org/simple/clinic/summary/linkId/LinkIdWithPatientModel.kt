package org.simple.clinic.summary.linkId

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

@Parcelize
data class LinkIdWithPatientModel(
    val patientUuid: UUID?,
    val identifier: Identifier?,
    val patientName: String?,
    val addButtonState: ButtonState?
) : Parcelable {

  companion object {

    fun create() = LinkIdWithPatientModel(
        patientUuid = null,
        identifier = null,
        patientName = null,
        addButtonState = null
    )
  }

  val hasPatientName: Boolean
    get() = patientName != null

  fun linkIdWithPatientViewShown(patientUuid: UUID, identifier: Identifier): LinkIdWithPatientModel {
    return copy(patientUuid = patientUuid, identifier = identifier)
  }

  fun patientNameFetched(patientName: String): LinkIdWithPatientModel {
    return copy(patientName = patientName)
  }

  fun linkingIdToPatient(): LinkIdWithPatientModel {
    return copy(addButtonState = ButtonState.SAVING)
  }

  fun linkedIdToPatient(): LinkIdWithPatientModel {
    return copy(addButtonState = ButtonState.SAVED)
  }
}
