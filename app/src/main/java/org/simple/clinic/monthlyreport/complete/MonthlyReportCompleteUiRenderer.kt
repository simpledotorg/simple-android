package org.simple.clinic.monthlyreport.complete

import org.simple.clinic.mobius.ViewRenderer

class MonthlyReportCompleteUiRenderer(
    private val ui: MonthlyReportCompleteUi
) : ViewRenderer<MonthlyReportCompleteModel> {
  override fun render(model: MonthlyReportCompleteModel) {

    if (model.questionnaireResponse != null) {
      ui.showFormSubmissionMonthAndYearTextView(model.questionnaireResponse)
    }
  }
}
