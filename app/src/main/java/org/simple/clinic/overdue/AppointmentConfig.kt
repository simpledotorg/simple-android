package org.simple.clinic.overdue

import org.threeten.bp.Period

data class AppointmentConfig(
    val appointmentDuePeriodForDefaulters: Period,
    val scheduleAppointmentsIn: List<TimeToAppointment>,
    val defaultTimeToAppointment: TimeToAppointment,
    val periodForIncludingOverdueAppointments: Period,
    val remindAppointmentsIn: List<TimeToAppointment>
)
