package org.simple.clinic.summary.nextappointment

import org.simple.clinic.overdue.Appointment
import org.simple.clinic.widgets.UiEvent

sealed class NextAppointmentEvent : UiEvent

data class NextAppointmentPatientProfileLoaded(
    val nextAppointmentPatientProfile: NextAppointmentPatientProfile?
) : NextAppointmentEvent()

data class NextAppointmentActionButtonClicked(
    override val analyticsName: String = "Next Appointment Card:Action Button Clicked"
) : NextAppointmentEvent()