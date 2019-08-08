package org.simple.clinic.scheduleappointment

// TODO: Merge with appointment config
data class ScheduleAppointmentConfig(
    val periodsToScheduleAppointmentsIn: List<ScheduleAppointmentIn>,
    val scheduleAppointmentInByDefault: ScheduleAppointmentIn
)
