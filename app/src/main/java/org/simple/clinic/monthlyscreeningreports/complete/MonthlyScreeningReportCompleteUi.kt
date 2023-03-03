package org.simple.clinic.monthlyscreeningreports.complete

import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

interface MonthlyScreeningReportCompleteUi {
  fun showMonthCompletedView(response: QuestionnaireResponse)
  fun goToMonthlyReportListScreen()
}
