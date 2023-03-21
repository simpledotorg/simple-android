package org.simple.clinic.monthlyscreeningreports.list

import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

sealed class MonthlyScreeningReportListEffect

object LoadCurrentFacility : MonthlyScreeningReportListEffect()

object LoadMonthlyScreeningReportListEffect : MonthlyScreeningReportListEffect()

sealed class MonthlyScreeningReportListViewEffect : MonthlyScreeningReportListEffect()

object GoBack : MonthlyScreeningReportListViewEffect()

data class OpenMonthlyScreeningForm(
    val questionnaireResponse: QuestionnaireResponse
) : MonthlyScreeningReportListViewEffect()


