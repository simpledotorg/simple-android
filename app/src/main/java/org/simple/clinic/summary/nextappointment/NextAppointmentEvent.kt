package org.simple.clinic.summary.nextappointment

import org.simple.clinic.overdue.Appointment

sealed class NextAppointmentEvent

data class NextAppointmentPatientProfileLoaded(
    val nextAppointmentPatientProfile: NextAppointmentPatientProfile?
) : NextAppointmentEvent()
