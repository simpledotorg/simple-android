package org.simple.clinic.patientcontact

import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.util.Optional
import java.util.UUID

data class PatientContactModel(
    private val patientUuid: UUID,
    private val patientProfile: PatientProfile? = null,
    private val appointment: Optional<OverdueAppointment>? = null
) {


  companion object {
    fun create(patientUuid: UUID): PatientContactModel = PatientContactModel(patientUuid)
  }

  fun patientProfileLoaded(patientProfile: PatientProfile): PatientContactModel {
    return copy(patientProfile = patientProfile)
  }

  fun overdueAppointmentLoaded(appointment: Optional<OverdueAppointment>): PatientContactModel {
    return copy(appointment = appointment)
  }
}
