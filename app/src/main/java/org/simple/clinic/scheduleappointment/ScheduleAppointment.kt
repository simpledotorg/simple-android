package org.simple.clinic.scheduleappointment

import org.threeten.bp.temporal.ChronoUnit

data class ScheduleAppointment(
    val timeAmount: Int,
    val chronoUnit: ChronoUnit
)
