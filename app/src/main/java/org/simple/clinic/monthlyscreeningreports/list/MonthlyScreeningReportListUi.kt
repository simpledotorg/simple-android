package org.simple.clinic.monthlyscreeningreports.list

import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

interface MonthlyScreeningReportListUi {
  fun setFacility(facilityName: String)
  fun displayMonthlyScreeningReportList(responseList: List<QuestionnaireResponse>)
  fun goBack()
  fun showProgress()
  fun hideProgress()
  fun openMonthlyScreeningForm(questionnaireResponse: QuestionnaireResponse)
}
