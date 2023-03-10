package org.simple.clinic.monthlyscreeningreports.complete

import java.util.UUID

sealed class MonthlyScreeningReportCompleteEffect

data class LoadQuestionnaireResponseEffect(val questionnaireId: UUID) : MonthlyScreeningReportCompleteEffect()

sealed class MonthlyScreeningReportCompleteViewEffect : MonthlyScreeningReportCompleteEffect()

object GoToMonthlyScreeningReportListScreen : MonthlyScreeningReportCompleteViewEffect()

