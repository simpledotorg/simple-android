package org.simple.clinic.removeoverdueappointment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.contactpatient.RemoveAppointmentReason
import org.simple.clinic.overdue.Appointment

@Parcelize
data class RemoveOverdueModel(
    val appointment: Appointment,
    val selectedReason: RemoveAppointmentReason?
) : Parcelable {

  val hasSelectedReason: Boolean
    get() = selectedReason != null

  companion object {

    fun create(appointment: Appointment) = RemoveOverdueModel(
        appointment = appointment,
        selectedReason = null
    )
  }

  fun removeAppointmentReasonSelected(selectedReason: RemoveAppointmentReason): RemoveOverdueModel {
    return copy(selectedReason = selectedReason)
  }
}
