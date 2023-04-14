package org.simple.clinic.monthlyreport.list

import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

sealed class MonthlyReportListEffect

object LoadCurrentFacility : MonthlyReportListEffect()

object LoadMonthlyReportListEffect : MonthlyReportListEffect()

sealed class MonthlyReportListViewEffect : MonthlyReportListEffect()

object GoBack : MonthlyReportListViewEffect()

data class OpenMonthlyReportForm(
    val questionnaireType: QuestionnaireType,
    val questionnaireResponse: QuestionnaireResponse
) : MonthlyReportListViewEffect()


