package org.simple.clinic.monthlyreport.complete

import java.util.UUID

sealed class MonthlyReportCompleteEffect

data class LoadQuestionnaireResponseEffect(val questionnaireId: UUID) : MonthlyReportCompleteEffect()

sealed class MonthlyReportCompleteViewEffect : MonthlyReportCompleteEffect()

object GoToMonthlyReportListScreen : MonthlyReportCompleteViewEffect()

