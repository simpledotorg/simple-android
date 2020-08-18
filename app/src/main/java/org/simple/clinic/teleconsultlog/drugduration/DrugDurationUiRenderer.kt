package org.simple.clinic.teleconsultlog.drugduration

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.teleconsultlog.drugduration.DrugDurationValidationResult.BLANK
import org.simple.clinic.teleconsultlog.drugduration.DrugDurationValidationResult.NOT_VALIDATED

class DrugDurationUiRenderer(private val ui: DrugDurationUi) : ViewRenderer<DrugDurationModel> {

  override fun render(model: DrugDurationModel) {
    if (model.hasValidationResult) {
      renderValidationResult(model.validationResult!!)
    }
  }

  private fun renderValidationResult(validationResult: DrugDurationValidationResult) {
    when (validationResult) {
      NOT_VALIDATED -> ui.hideDurationError()
      BLANK -> ui.showBlankDurationError()
    }
  }
}
