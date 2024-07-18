package org.simple.clinic.monthlyreports.list

import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

sealed class MonthlyReportsEffect

data object LoadCurrentFacility : MonthlyReportsEffect()

data class LoadMonthlyReportsEffect(
    val questionnaireType: QuestionnaireType
) : MonthlyReportsEffect()

sealed class MonthlyReportsViewEffect : MonthlyReportsEffect()

data object GoBack : MonthlyReportsViewEffect()

data class OpenMonthlyReportForm(
    val questionnaireType: QuestionnaireType,
    val questionnaireResponse: QuestionnaireResponse
) : MonthlyReportsViewEffect()


