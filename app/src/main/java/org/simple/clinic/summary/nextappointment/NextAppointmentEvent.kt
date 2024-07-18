package org.simple.clinic.summary.nextappointment

import org.simple.clinic.widgets.UiEvent

sealed class NextAppointmentEvent : UiEvent

data class NextAppointmentPatientProfileLoaded(
    val nextAppointmentPatientProfile: NextAppointmentPatientProfile?
) : NextAppointmentEvent()

data object RefreshAppointment : NextAppointmentEvent()
