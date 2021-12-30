package org.simple.clinic.summary.nextappointment

import org.simple.clinic.overdue.Appointment
import org.simple.clinic.patient.PatientAndAssignedFacility
import java.util.UUID

data class NextAppointmentModel(
    val patientUuid: UUID,
    val appointment: Appointment?,
    val patientAndAssignedFacility: PatientAndAssignedFacility?
) {

  val hasPatientAndAssignedFacility: Boolean
    get() = patientAndAssignedFacility != null

  val hasAppointment: Boolean
    get() = appointment != null

  companion object {
    fun default(patientUuid: UUID) = NextAppointmentModel(
        patientUuid = patientUuid,
        appointment = null,
        patientAndAssignedFacility = null
    )
  }

  fun appointmentLoaded(appointment: Appointment?): NextAppointmentModel {
    return copy(appointment = appointment)
  }

  fun patientAndAssignedFacilityLoaded(patientAndAssignedFacility: PatientAndAssignedFacility): NextAppointmentModel {
    return copy(patientAndAssignedFacility = patientAndAssignedFacility)
  }
}
