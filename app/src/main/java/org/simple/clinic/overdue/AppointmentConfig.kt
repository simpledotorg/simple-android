package org.simple.clinic.overdue

import org.threeten.bp.Period

data class AppointmentConfig(
    val v2ApiEnabled: Boolean,
    val minimumOverduePeriodForHighRisk: Period,
    val overduePeriodForLowestRiskLevel: Period
)
