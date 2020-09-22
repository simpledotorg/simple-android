package org.simple.clinic.teleconsultlog.drugduration

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class DrugDurationUiRenderer(private val ui: DrugDurationUi) : ViewRenderer<DrugDurationModel> {

  private val durationCallback = ValueChangedCallback<String>()

  override fun render(model: DrugDurationModel) {
    if (model.hasValidationResult) {
      renderValidationResult(model.validationResult!!)
    } else {
      ui.hideDurationError()
    }

    renderDrugDuration(model)
  }

  private fun renderDrugDuration(model: DrugDurationModel) {
    durationCallback.pass(model.duration, ui::setDrugDuration)
  }

  private fun renderValidationResult(validationResult: DrugDurationValidationResult) {
    when (validationResult) {
      Blank -> ui.showBlankDurationError()
    }
  }
}
