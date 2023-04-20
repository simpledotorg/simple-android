package org.simple.clinic.monthlyreports.complete

import java.util.UUID

sealed class MonthlyReportCompleteEffect

data class LoadQuestionnaireResponseEffect(val questionnaireId: UUID) : MonthlyReportCompleteEffect()

sealed class MonthlyReportCompleteViewEffect : MonthlyReportCompleteEffect()

object GoToMonthlyReportListScreen : MonthlyReportCompleteViewEffect()

