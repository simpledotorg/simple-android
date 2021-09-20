package org.simple.clinic.removeoverdueappointment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.contactpatient.RemoveAppointmentReason
import org.simple.clinic.overdue.Appointment
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

    fun create(appointment: Appointment) = RemoveOverdueModel(
        appointmentId = appointment.uuid,
        patientId = appointment.patientUuid,
        selectedReason = null
    )
  }

  fun removeAppointmentReasonSelected(selectedReason: RemoveAppointmentReason): RemoveOverdueModel {
    return copy(selectedReason = selectedReason)
  }
}
