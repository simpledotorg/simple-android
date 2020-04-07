package org.simple.clinic.patientcontact

import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.util.Optional
import java.util.UUID

data class PatientContactModel(
    val patientUuid: UUID,
    val patientProfile: PatientProfile? = null,
    val appointment: Optional<OverdueAppointment>? = null
) {

  companion object {
    fun create(patientUuid: UUID): PatientContactModel = PatientContactModel(patientUuid)
  }

  val hasLoadedPatientProfile: Boolean
    get() = patientProfile != null

  val hasLoadedAppointment: Boolean
    get() = appointment != null

  fun patientProfileLoaded(patientProfile: PatientProfile): PatientContactModel {
    return copy(patientProfile = patientProfile)
  }

  fun overdueAppointmentLoaded(appointment: Optional<OverdueAppointment>): PatientContactModel {
    return copy(appointment = appointment)
  }
}
