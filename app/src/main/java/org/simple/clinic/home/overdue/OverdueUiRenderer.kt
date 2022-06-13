package org.simple.clinic.home.overdue

import org.simple.clinic.mobius.ViewRenderer

class OverdueUiRenderer(
    private val ui: OverdueUi,
    private val isOverdueSectionsFeatureEnabled: Boolean
) : ViewRenderer<OverdueModel> {

  override fun render(model: OverdueModel) {
    if (isOverdueSectionsFeatureEnabled) {
      loadOverdueSections(model)
    }
  }

  private fun loadOverdueSections(model: OverdueModel) {
    if (model.hasLoadedOverdueAppointments) {
      ui.showOverdueAppointments(
        model.overdueAppointmentSections!!,
        model.overdueListSectionStates
      )
      ui.showOverdueCount(model.overdueCount)
      ui.hideProgress()
      renderOverdueListLoadedViews(model)
    } else {
      renderOverdueListLoadingViews()
    }
  }

  private fun renderOverdueListLoadingViews() {
    ui.showProgress()
    ui.hideOverdueRecyclerView()
    ui.hideNoOverduePatientsView()
  }

  private fun renderOverdueListLoadedViews(model: OverdueModel) {
    if (model.isOverdueAppointmentSectionsListEmpty) {
      ui.showNoOverduePatientsView()
      ui.hideOverdueRecyclerView()
    } else {
      ui.hideNoOverduePatientsView()
      ui.showOverdueRecyclerView()
    }
  }
}
