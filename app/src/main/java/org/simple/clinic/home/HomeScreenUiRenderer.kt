package org.simple.clinic.home

import org.simple.clinic.mobius.ViewRenderer

class HomeScreenUiRenderer(private val ui: HomeScreenUi) : ViewRenderer<HomeScreenModel> {

  override fun render(model: HomeScreenModel) {
    if (model.hasFacility) {
      renderFacility(model)
    }

    if (model.hasOverdueAppointmentCount) {
      renderOverdueAppointmentCount(model)
    }
  }

  private fun renderOverdueAppointmentCount(model: HomeScreenModel) {
    val overdueAppointmentCount = model.overdueAppointmentCount!!
    if (overdueAppointmentCount > 0)
      ui.showOverdueAppointmentCount(overdueAppointmentCount)
    else
      ui.removeOverdueAppointmentCount()
  }

  private fun renderFacility(model: HomeScreenModel) {
    ui.setFacility(model.facility!!.name)
  }
}
