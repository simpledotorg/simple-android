package org.simple.clinic.monthlyreport.list

import org.simple.clinic.mobius.ViewRenderer

class MonthlyReportListUiRenderer(private val ui: MonthlyReportListUi) :
    ViewRenderer<MonthlyReportListModel> {
  override fun render(model: MonthlyReportListModel) {
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

  private fun renderFacility(model: MonthlyReportListModel) {
    ui.setFacility(model.facility!!.name)
  }
}
