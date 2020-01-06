package org.simple.clinic.summary.bloodsugar

import org.simple.clinic.mobius.ViewRenderer

class BloodSugarSummaryViewUiRenderer(private val ui: BloodSugarSummaryViewUi) : ViewRenderer<BloodSugarSummaryViewModel> {

  override fun render(model: BloodSugarSummaryViewModel) {
    when {
      model.measurements == null -> return
      model.measurements.isEmpty() -> ui.showNoBloodSugarsView()
      else -> ui.showBloodSugarSummary(model.measurements)
    }
  }
}
