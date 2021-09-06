package org.simple.clinic.selectstate

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.selectstate.StatesFetchError.NetworkError
import org.simple.clinic.selectstate.StatesFetchError.ServerError

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

    when (model.statesFetchError) {
      NetworkError -> ui.showNetworkErrorMessage()
      ServerError -> ui.showServerErrorMessage()
    }
  }

  private fun renderNextButton(model: SelectStateModel) {
    if (model.hasSelectedState) ui.showNextButton()
  }
}
