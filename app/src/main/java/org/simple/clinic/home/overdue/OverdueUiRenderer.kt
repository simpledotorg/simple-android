package org.simple.clinic.home.overdue

import org.simple.clinic.mobius.ViewRenderer

class OverdueUiRenderer(
    private val ui: OverdueUi
) : ViewRenderer<OverdueModel> {

  override fun render(model: OverdueModel) {
    if (model.hasLoadedOverdueAppointments) {
      renderOverdueAppointments(model.overdueAppointments!!)
    }
  }

  private fun renderOverdueAppointments(overdueAppointments: List<OverdueAppointment>) {
    ui.handleEmptyList(overdueAppointments.isEmpty())
  }
}
