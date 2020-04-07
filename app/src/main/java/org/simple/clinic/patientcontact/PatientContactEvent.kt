package org.simple.clinic.patientcontact

import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.util.Optional

sealed class PatientContactEvent

data class PatientProfileLoaded(
    val patientProfile: PatientProfile
) : PatientContactEvent()

data class OverdueAppointmentLoaded(
    val overdueAppointment: Optional<OverdueAppointment>
) : PatientContactEvent()
