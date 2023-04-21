package org.simple.clinic.monthlyreports.list

import org.simple.clinic.mobius.ViewRenderer

class MonthlyReportsUiRenderer(private val ui: MonthlyReportsUi) :
    ViewRenderer<MonthlyReportsModel> {
  override fun render(model: MonthlyReportsModel) {
    val monthlyReports = model.questionnaireResponses

    if (!monthlyReports.isNullOrEmpty()) {
      ui.displayMonthlyReports(monthlyReports)
    } else {
      //no-op
    }
    if (model.hasFacility) {
      renderFacility(model)
    }
  }

  private fun renderFacility(model: MonthlyReportsModel) {
    ui.setFacility(model.facility!!.name)
  }
}
