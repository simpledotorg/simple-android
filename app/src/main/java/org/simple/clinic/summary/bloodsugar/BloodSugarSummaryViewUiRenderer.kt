package org.simple.clinic.summary.bloodsugar

import org.simple.clinic.mobius.ViewRenderer

class BloodSugarSummaryViewUiRenderer(
    private val ui: BloodSugarSummaryViewUi,
    private val config: BloodSugarSummaryConfig
) : ViewRenderer<BloodSugarSummaryViewModel> {

  override fun render(model: BloodSugarSummaryViewModel) {
    when {
      model.measurements == null -> return
      model.measurements.isEmpty() -> ui.showNoBloodSugarsView()
      else -> ui.showBloodSugarSummary(model.measurements)
    }

    if (model.totalRecordedBloodSugarCount != null && model.totalRecordedBloodSugarCount > config.numberOfBloodSugarsToDisplay) {
      ui.showSeeAllButton()
    } else {
      ui.hideSeeAllButton()
    }
  }
}
