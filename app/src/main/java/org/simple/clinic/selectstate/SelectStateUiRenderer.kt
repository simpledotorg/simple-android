package org.simple.clinic.selectstate

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.selectstate.StatesFetchError.NetworkError
import org.simple.clinic.selectstate.StatesFetchError.ServerError
import org.simple.clinic.selectstate.StatesFetchError.UnexpectedError

class SelectStateUiRenderer(
    private val ui: SelectStateUi
) : ViewRenderer<SelectStateModel> {

  override fun render(model: SelectStateModel) {
    if (model.isFetching) {
      ui.showProgress()
    } else {
      ui.hideProgress()
    }

    if (model.hasMoreThanOneState) {
      ui.showStates(model.states!!, model.selectedState)
    } else {
      ui.hideStates()
    }

    if (model.hasFetchError) {
      renderErrorView(model)
    } else {
      ui.hideErrorView()
    }
  }

  private fun renderErrorView(model: SelectStateModel) {
    when (model.statesFetchError) {
      NetworkError -> ui.showNetworkErrorMessage()
      ServerError -> ui.showServerErrorMessage()
      UnexpectedError -> ui.showGenericErrorMessage()
    }
  }
}
