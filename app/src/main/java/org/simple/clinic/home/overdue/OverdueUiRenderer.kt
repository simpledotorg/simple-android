package org.simple.clinic.home.overdue

import org.simple.clinic.mobius.ViewRenderer

class OverdueUiRenderer(
    private val ui: OverdueUi,
    private val isOverdueSectionsFeatureEnabled: Boolean
) : ViewRenderer<OverdueModel> {

  override fun render(model: OverdueModel) {
    if (model.hasLoadedOverdueAppointments && isOverdueSectionsFeatureEnabled) {
      ui.showOverdueAppointments(
          pendingAppointments = model.pendingAppointments.orEmpty(),
          agreedToVisitAppointments = model.agreedToVisitAppointments.orEmpty(),
          remindToCallLaterAppointments = model.remindToCallLaterAppointments.orEmpty(),
          removedFromOverdueAppointments = model.removedFromOverdueAppointments.orEmpty(),
          moreThanAnYearOverdueAppointments = model.moreThanAnYearOverdueAppointments.orEmpty()
      )
    }
  }
}
