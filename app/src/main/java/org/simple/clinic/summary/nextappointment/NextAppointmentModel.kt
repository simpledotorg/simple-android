package org.simple.clinic.summary.nextappointment

import org.simple.clinic.overdue.Appointment
import java.util.UUID

data class NextAppointmentModel(
    val patientUuid: UUID,
    val appointment: Appointment?
) {

  val hasAppointment: Boolean
    get() = appointment != null

  companion object {
    fun default(patientUuid: UUID) = NextAppointmentModel(
        patientUuid = patientUuid,
        appointment = null
    )
  }

  fun appointmentLoaded(appointment: Appointment?): NextAppointmentModel {
    return copy(appointment = appointment)
  }
}
