package org.simple.clinic.home.overdue

import org.simple.clinic.mobius.ViewRenderer

class OverdueUiRenderer(
    private val ui: OverdueUi
) : ViewRenderer<OverdueModel> {

  override fun render(model: OverdueModel) {
    if (model.hasLoadedOverdueAppointments) {
      ui.updateList(model.overdueAppointments!!, isDiabetesManagementEnabled = model.isDiabetesManagementEnabled)
    }
  }
}
