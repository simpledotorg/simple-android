package org.simple.clinic.monthlyreports.list

import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

sealed class MonthlyReportsEffect

object LoadCurrentFacility : MonthlyReportsEffect()

data class LoadMonthlyReportsEffect(
    val questionnaireType: QuestionnaireType
) : MonthlyReportsEffect()

sealed class MonthlyReportsViewEffect : MonthlyReportsEffect()

object GoBack : MonthlyReportsViewEffect()

data class OpenMonthlyReportForm(
    val questionnaireType: QuestionnaireType,
    val questionnaireResponse: QuestionnaireResponse
) : MonthlyReportsViewEffect()


