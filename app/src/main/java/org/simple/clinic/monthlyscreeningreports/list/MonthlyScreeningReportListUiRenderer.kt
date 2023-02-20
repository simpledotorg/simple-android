package org.simple.clinic.monthlyscreeningreports.list

import org.simple.clinic.mobius.ViewRenderer

class MonthlyScreeningReportListUiRenderer(private val ui: MonthlyScreeningReportListUi) :
    ViewRenderer<MonthlyScreeningReportListModel> {
  override fun render(model: MonthlyScreeningReportListModel) {
    val responseList = model.questionnaireResponses

    if (!responseList.isNullOrEmpty()) {
      ui.displayMonthlyReportList(responseList)
    } else {
      //no-op
    }
    if (model.hasFacility) {
      renderFacility(model)
    }
  }

  private fun renderFacility(model: MonthlyScreeningReportListModel) {
    ui.setFacility(model.facility!!.name)
  }
}
