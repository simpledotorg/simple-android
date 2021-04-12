package org.simple.clinic.removeoverdueappointment

import org.simple.clinic.contactpatient.RemoveAppointmentReason
import java.util.UUID

data class RemoveOverdueModel(
    val appointmentId: UUID,
    val selectedReason: RemoveAppointmentReason?
) {

  val hasSelectedReason: Boolean
    get() = selectedReason != null

  companion object {

    fun create(appointmentId: UUID) = RemoveOverdueModel(
        appointmentId = appointmentId,
        selectedReason = null
    )
  }

  fun removeAppointmentReasonSelected(selectedReason: RemoveAppointmentReason): RemoveOverdueModel {
    return copy(selectedReason = selectedReason)
  }
}
