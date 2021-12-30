package org.simple.clinic.summary.nextappointment

import org.simple.clinic.overdue.Appointment
import org.simple.clinic.patient.PatientAndAssignedFacility

sealed class NextAppointmentEvent

data class AppointmentLoaded(val appointment: Appointment?) : NextAppointmentEvent()

data class PatientAndAssignedFacilityLoaded(val patientAndAssignedFacility: PatientAndAssignedFacility) : NextAppointmentEvent()
