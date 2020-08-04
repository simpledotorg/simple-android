package org.simple.clinic.summary.prescribeddrugs

import org.simple.clinic.mobius.ViewRenderer

class DrugSummaryUiRenderer(
    private val ui: DrugSummaryUi
) : ViewRenderer<DrugSummaryModel> {

  override fun render(model: DrugSummaryModel) {
    if (model.hasPrescribedDrugs) {
      ui.populatePrescribedDrugs(model.prescribedDrugs!!)
    }
  }
}
