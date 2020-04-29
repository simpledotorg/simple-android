package org.simple.clinic.summary.bloodpressures

import org.simple.clinic.mobius.ViewRenderer

class BloodPressureSummaryViewUiRenderer(
    private val ui: BloodPressureSummaryViewUi,
    private val config: BloodPressureSummaryViewConfig
) : ViewRenderer<BloodPressureSummaryViewModel> {
  override fun render(model: BloodPressureSummaryViewModel) {
    when {
      model.latestBloodPressuresToDisplay == null -> return
      model.latestBloodPressuresToDisplay.isEmpty() -> ui.showNoBloodPressuresView()
      else -> ui.showBloodPressures(model.latestBloodPressuresToDisplay)
    }

    if (model.hasLoadedFacility && model.hasLoadedCountOfBloodSugars) {
      val numberOfBpsToDisplay = if (model.isDiabetesManagementEnabled) {
        config.numberOfBpsToDisplay
      } else {
        config.numberOfBpsToDisplayWithoutDiabetesManagement
      }

      when {
        model.totalRecordedBloodPressureCount!! > numberOfBpsToDisplay -> ui.showSeeAllButton()
        else -> ui.hideSeeAllButton()
      }
    }
  }
}
