package org.simple.clinic.monthlyreports.complete

import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

interface MonthlyReportCompleteUi {
  fun showFormSubmissionMonthAndYearTextView(response: QuestionnaireResponse)
  fun goToMonthlyReportListScreen()
}
