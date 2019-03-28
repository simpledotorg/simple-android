package org.simple.clinic.overdue

import org.threeten.bp.Period

data class AppointmentConfig(
    val minimumOverduePeriodForHighRisk: Period,
    val overduePeriodForLowestRiskLevel: Period,
    val isApiV3Enabled: Boolean
)
