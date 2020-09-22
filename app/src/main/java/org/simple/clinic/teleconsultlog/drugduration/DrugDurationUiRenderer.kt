package org.simple.clinic.teleconsultlog.drugduration

import org.simple.clinic.mobius.ViewRenderer

class DrugDurationUiRenderer(private val ui: DrugDurationUi) : ViewRenderer<DrugDurationModel> {

  override fun render(model: DrugDurationModel) {
    if (model.hasValidationResult) {
      renderValidationResult(model.validationResult!!)
    } else {
      ui.hideDurationError()
    }
  }

  private fun renderValidationResult(validationResult: DrugDurationValidationResult) {
    when (validationResult) {
      Blank -> ui.showBlankDurationError()
    }
  }
}
