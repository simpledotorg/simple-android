package org.simple.clinic.monthlyscreeningreports.list

import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import java.util.UUID

interface MonthlyScreeningReportListUi {
  fun setFacility(facilityName: String)
  fun displayMonthlyReportList(responseList: List<QuestionnaireResponse>)
  fun goBack()
  fun showProgress()
  fun hideProgress()
  fun openMonthlyScreeningForm(uuid: UUID)
}
