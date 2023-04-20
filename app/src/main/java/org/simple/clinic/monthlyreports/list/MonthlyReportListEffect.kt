package org.simple.clinic.monthlyreports.list

import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

sealed class MonthlyReportListEffect

object LoadCurrentFacility : MonthlyReportListEffect()

data class LoadMonthlyReportListEffect(
    val questionnaireType: QuestionnaireType
) : MonthlyReportListEffect()

sealed class MonthlyReportListViewEffect : MonthlyReportListEffect()

object GoBack : MonthlyReportListViewEffect()

data class OpenMonthlyReportForm(
    val questionnaireType: QuestionnaireType,
    val questionnaireResponse: QuestionnaireResponse
) : MonthlyReportListViewEffect()


