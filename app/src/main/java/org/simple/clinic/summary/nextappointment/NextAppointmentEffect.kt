package org.simple.clinic.summary.nextappointment

import java.util.UUID

sealed class NextAppointmentEffect

data class LoadNextAppointmentPatientProfile(val patientUuid: UUID) : NextAppointmentEffect()

data class OpenScheduleAppointmentSheet(val patientUuid: UUID) : NextAppointmentEffect()
