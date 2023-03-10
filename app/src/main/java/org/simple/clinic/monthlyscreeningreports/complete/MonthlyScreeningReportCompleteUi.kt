package org.simple.clinic.monthlyscreeningreports.complete

import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

interface MonthlyScreeningReportCompleteUi {
  fun showCompletedDateTimeTextView(response: QuestionnaireResponse)
  fun goToMonthlyScreeningReportListScreen()
}
