package org.simple.clinic.scheduleappointment

import org.threeten.bp.temporal.ChronoUnit

data class ScheduleAppointment(
    val displayText: String,
    val timeAmount: Int,
    val chronoUnit: ChronoUnit
)
