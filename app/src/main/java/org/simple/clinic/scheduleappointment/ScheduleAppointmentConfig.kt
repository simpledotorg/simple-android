package org.simple.clinic.scheduleappointment

// TODO: Merge with appointment config
data class ScheduleAppointmentConfig(
    val possibleAppointments: List<ScheduleAppointment>,
    val defaultAppointment: ScheduleAppointment
)
