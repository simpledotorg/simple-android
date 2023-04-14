package org.simple.clinic.monthlyreport.complete

import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

interface MonthlyReportCompleteUi {
  fun showFormSubmissionMonthAndYearTextView(response: QuestionnaireResponse)
  fun goToMonthlyReportListScreen()
}
