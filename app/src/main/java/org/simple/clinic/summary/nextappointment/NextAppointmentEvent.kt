package org.simple.clinic.summary.nextappointment

sealed class NextAppointmentEvent

data class NextAppointmentPatientProfileLoaded(
    val nextAppointmentPatientProfile: NextAppointmentPatientProfile?
) : NextAppointmentEvent()

object NextAppointmentActionButtonClicked : NextAppointmentEvent()
