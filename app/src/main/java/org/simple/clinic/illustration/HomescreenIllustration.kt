package org.simple.clinic.illustration

data class HomescreenIllustration(
    val eventId: String,
    val illustrationUrl: String,
    val from: DayOfMonth,
    val to: DayOfMonth
)
