package org.simple.clinic.selectstate

import org.simple.clinic.mobius.ViewRenderer

class SelectStateUiRenderer(
    private val ui: SelectStateUi
) : ViewRenderer<SelectStateModel> {

  override fun render(model: SelectStateModel) {
    if (model.hasStates) {
      ui.showStates(model.states!!, model.selectedState)
      renderNextButton(model)
    } else {
      ui.hideStates()
    }
  }

  private fun renderNextButton(model: SelectStateModel) {
    if (model.hasSelectedState) ui.showNextButton()
  }
}
