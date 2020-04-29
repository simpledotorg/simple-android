package org.simple.clinic.home.overdue.appointmentreminder

import org.threeten.bp.temporal.ChronoUnit

data class AppointmentReminder(val timeAmount: Int, val chronoUnit: ChronoUnit)
