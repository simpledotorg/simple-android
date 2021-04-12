package org.simple.clinic.removeoverdueappointment

import org.simple.clinic.contactpatient.RemoveAppointmentReason

data class RemoveOverdueModel(
    val selectedReason: RemoveAppointmentReason?
) {

  companion object {

    fun create() = RemoveOverdueModel(
        selectedReason = null
    )
  }

  fun removeAppointmentReasonSelected(selectedReason: RemoveAppointmentReason): RemoveOverdueModel {
    return copy(selectedReason = selectedReason)
  }
}
