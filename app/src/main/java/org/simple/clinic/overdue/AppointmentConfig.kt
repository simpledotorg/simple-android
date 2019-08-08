package org.simple.clinic.overdue

import org.simple.clinic.scheduleappointment.ScheduleAppointmentIn
import org.threeten.bp.Period

data class AppointmentConfig(
    val minimumOverduePeriodForHighRisk: Period,
    val overduePeriodForLowestRiskLevel: Period,
    val appointmentDuePeriodForDefaulters: Period,
    val periodsToScheduleAppointmentsIn: List<ScheduleAppointmentIn>,
    val scheduleAppointmentInByDefault: ScheduleAppointmentIn
)
