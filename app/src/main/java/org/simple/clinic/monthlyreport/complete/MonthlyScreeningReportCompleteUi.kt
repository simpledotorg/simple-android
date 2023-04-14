package org.simple.clinic.monthlyreport.complete

import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

interface MonthlyScreeningReportCompleteUi {
  fun showFormSubmissionMonthAndYearTextView(response: QuestionnaireResponse)
  fun goToMonthlyScreeningReportListScreen()
}
