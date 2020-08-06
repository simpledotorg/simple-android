package org.simple.clinic.home.help

import org.simple.clinic.help.HelpPullResult.NetworkError
import org.simple.clinic.help.HelpPullResult.OtherError
import org.simple.clinic.mobius.ViewRenderer

class HelpScreenUiRenderer(private val ui: HelpScreenUi) : ViewRenderer<HelpScreenModel> {
  override fun render(model: HelpScreenModel) {
    if (model.hasHelpContent) {
      ui.showHelp(model.helpContent!!)
    } else {
      renderHelpError(model)
    }
  }

  private fun renderHelpError(model: HelpScreenModel) {
    ui.showNoHelpAvailable()
    if (model.hasHelpPullResult) {
      renderHelpPullResultError(model)
    }
  }

  private fun renderHelpPullResultError(model: HelpScreenModel) {
    when (model.helpPullResult!!) {
      NetworkError -> ui.showNetworkErrorMessage()
      OtherError -> ui.showUnexpectedErrorMessage()
    }
  }
}
