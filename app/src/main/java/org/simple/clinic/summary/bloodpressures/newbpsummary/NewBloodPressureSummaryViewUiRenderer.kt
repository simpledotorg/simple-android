package org.simple.clinic.summary.bloodpressures.newbpsummary

import org.simple.clinic.mobius.ViewRenderer

class NewBloodPressureSummaryViewUiRenderer(
    private val ui: NewBloodPressureSummaryViewUi,
    private val config: NewBloodPressureSummaryViewConfig
) : ViewRenderer<NewBloodPressureSummaryViewModel> {
  override fun render(model: NewBloodPressureSummaryViewModel) {
    when {
      model.latestBloodPressuresToDisplay == null -> return
      model.latestBloodPressuresToDisplay.isEmpty() -> ui.showNoBloodPressuresView()
      else -> ui.showBloodPressures(model.latestBloodPressuresToDisplay)
    }

    if (model.totalRecordedBloodPressureCount != null && model.totalRecordedBloodPressureCount > config.numberOfBpsToDisplay) {
      ui.showSeeAllButton()
    } else {
      ui.hideSeeAllButton()
    }
  }
}
