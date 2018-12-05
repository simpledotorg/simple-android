package org.simple.clinic.overdue

data class AppointmentConfig(
    val highlightHighRiskPatients: Boolean,
    val v2ApiEnabled: Boolean
)
