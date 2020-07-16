package org.simple.clinic.home.report

import org.simple.clinic.mobius.ViewRenderer

class ReportsUiRenderer(
    private val ui: ReportsUi
) : ViewRenderer<ReportsModel> {

  override fun render(model: ReportsModel) {
    if (model.hasLoadedReports) {
      renderReportsContent(model.reportsContent!!)
    }
  }

  private fun renderReportsContent(reportsContent: String) {
    if (reportsContent.isBlank()) {
      ui.showNoReportsAvailable()
    } else {
      ui.showReport(reportsContent)
    }
  }
}
