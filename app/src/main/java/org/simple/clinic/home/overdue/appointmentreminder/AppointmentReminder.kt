package org.simple.clinic.home.overdue.appointmentreminder

import org.threeten.bp.temporal.ChronoUnit

data class AppointmentReminder(val displayText: String, val timeAmount: Int, val chronoUnit: ChronoUnit)
