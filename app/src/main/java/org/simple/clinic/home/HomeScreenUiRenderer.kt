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
    if (model.hasAtLeastOneOverdueAppointment)
      ui.showOverdueAppointmentCount(model.overdueAppointmentCount!!)
    else
      ui.removeOverdueAppointmentCount()
  }

  private fun renderFacility(model: HomeScreenModel) {
    ui.setFacility(model.facility!!.name)
  }
}
