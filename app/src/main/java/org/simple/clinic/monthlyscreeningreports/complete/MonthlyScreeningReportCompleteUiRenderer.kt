package org.simple.clinic.monthlyscreeningreports.complete

import org.simple.clinic.mobius.ViewRenderer

class MonthlyScreeningReportCompleteUiRenderer(
    private val ui: MonthlyScreeningReportCompleteUi
) : ViewRenderer<MonthlyScreeningReportCompleteModel> {
  override fun render(model: MonthlyScreeningReportCompleteModel) {

    if (model.questionnaireResponse != null) {
      ui.showMonthCompletedView(model.questionnaireResponse)
    }
  }
}
