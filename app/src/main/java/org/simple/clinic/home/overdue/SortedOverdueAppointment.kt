package org.simple.clinic.home.overdue

data class SortedOverdueAppointment(
    val appointment: OverdueAppointment,
    val score: Float,
    val bucket: OverdueBucket
)
