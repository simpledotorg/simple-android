package org.simple.clinic.summary.nextappointment

import org.simple.clinic.overdue.Appointment

sealed class NextAppointmentEvent

data class AppointmentLoaded(val appointment: Appointment?) : NextAppointmentEvent()
