package org.simple.clinic.removeoverdueappointment

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.contactpatient.RemoveAppointmentReason
import java.util.UUID

@Parcelize
data class RemoveOverdueModel(
    val appointmentId: UUID,
    val patientId: UUID,
    val selectedReason: RemoveAppointmentReason?
) : Parcelable {

  val hasSelectedReason: Boolean
    get() = selectedReason != null

  companion object {

    fun create(appointmentId: UUID, patientId: UUID) = RemoveOverdueModel(
        appointmentId = appointmentId,
        patientId = patientId,
        selectedReason = null
    )
  }

  fun removeAppointmentReasonSelected(selectedReason: RemoveAppointmentReason): RemoveOverdueModel {
    return copy(selectedReason = selectedReason)
  }
}
