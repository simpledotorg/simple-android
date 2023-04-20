package org.simple.clinic.monthlyreports.list

import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

interface MonthlyReportsUi {
  fun setFacility(facilityName: String)
  fun displayMonthlyReports(monthlyReports: List<QuestionnaireResponse>)
  fun goBack()
  fun openMonthlyReportForm(questionnaireType: QuestionnaireType, questionnaireResponse: QuestionnaireResponse)
}
