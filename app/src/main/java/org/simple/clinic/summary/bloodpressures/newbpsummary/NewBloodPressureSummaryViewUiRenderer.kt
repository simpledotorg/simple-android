package org.simple.clinic.summary.bloodpressures.newbpsummary

import org.simple.clinic.mobius.ViewRenderer

class NewBloodPressureSummaryViewUiRenderer(
    private val ui: NewBloodPressureSummaryViewUi
) : ViewRenderer<NewBloodPressureSummaryViewModel> {
  override fun render(model: NewBloodPressureSummaryViewModel) {
    when {
      model.latestBloodPressuresToDisplay == null -> return
      model.latestBloodPressuresToDisplay.isEmpty() -> ui.showNoBloodPressuresView()
    }
  }
}
