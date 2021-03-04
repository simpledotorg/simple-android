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

    fun create(patientUuid: UUID, identifier: Identifier) = LinkIdWithPatientModel(
        patientUuid = patientUuid,
        identifier = identifier,
        patientName = null,
        addButtonState = null
    )
  }

  val hasPatientName: Boolean
    get() = patientName != null

  val addingIdToPatient: Boolean
    get() = addButtonState == ButtonState.SAVING

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
