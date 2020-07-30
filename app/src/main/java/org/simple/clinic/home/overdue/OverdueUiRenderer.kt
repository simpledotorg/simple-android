package org.simple.clinic.home.overdue

import org.simple.clinic.mobius.ViewRenderer

class OverdueUiRenderer(
    private val ui: OverdueUi
) : ViewRenderer<OverdueModel> {

  override fun render(model: OverdueModel) {
    if (model.hasLoadedOverdueAppointments) {
      renderOverdueAppointments(model.overdueAppointments!!, model.isDiabetesManagementEnabled)
    }
  }

  private fun renderOverdueAppointments(
      overdueAppointments: List<OverdueAppointment>,
      diabetesManagementEnabled: Boolean
  ) {
    ui.handleEmptyList(overdueAppointments.isEmpty())
    ui.updateList(overdueAppointments, isDiabetesManagementEnabled = diabetesManagementEnabled)
  }
}
