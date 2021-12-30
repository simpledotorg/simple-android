package org.simple.clinic.summary.nextappointment

import java.util.UUID

sealed class NextAppointmentEffect

data class LoadAppointment(val patientUuid: UUID) : NextAppointmentEffect()

data class LoadPatientAndAssignedFacility(val patientUuid: UUID) : NextAppointmentEffect()

sealed class NextAppointmentViewEffect : NextAppointmentEffect()

data class OpenScheduleAppointmentSheet(val patientUuid: UUID) : NextAppointmentViewEffect()
