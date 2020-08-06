package org.simple.clinic.home

import org.simple.clinic.mobius.ViewRenderer

class HomeScreenUiRenderer(private val ui: HomeScreenUi) : ViewRenderer<HomeScreenModel> {

  override fun render(model: HomeScreenModel) {
    if (model.hasFacility) {
      renderFacility(model)
    }
  }

  private fun renderFacility(model: HomeScreenModel) {
    ui.setFacility(model.facility!!.name)
  }
}
