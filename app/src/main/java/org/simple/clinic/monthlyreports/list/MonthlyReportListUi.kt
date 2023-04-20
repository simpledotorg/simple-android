package org.simple.clinic.monthlyreports.list

import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

interface MonthlyReportListUi {
  fun setFacility(facilityName: String)
  fun displayMonthlyReportList(responseList: List<QuestionnaireResponse>)
  fun goBack()
  fun showProgress()
  fun hideProgress()
  fun openMonthlyReportForm(questionnaireType: QuestionnaireType, questionnaireResponse: QuestionnaireResponse)
}
