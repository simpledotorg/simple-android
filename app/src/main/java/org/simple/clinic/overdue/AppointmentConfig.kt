package org.simple.clinic.overdue

import org.simple.clinic.scheduleappointment.TimeToAppointment
import org.threeten.bp.Period

data class AppointmentConfig(
    val minimumOverduePeriodForHighRisk: Period,
    val overduePeriodForLowestRiskLevel: Period,
    val appointmentDuePeriodForDefaulters: Period,
    val scheduleAppointmentsIn: List<TimeToAppointment>,
    val defaultTimeToAppointment: TimeToAppointment
)
