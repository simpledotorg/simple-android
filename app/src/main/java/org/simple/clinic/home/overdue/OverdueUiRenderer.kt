package org.simple.clinic.home.overdue

import org.simple.clinic.mobius.ViewRenderer
import java.util.UUID

class OverdueUiRenderer(private val ui: OverdueUi) : ViewRenderer<OverdueModel> {

  override fun render(model: OverdueModel) {
    loadOverdueSections(model)
  }

  private fun loadOverdueSections(model: OverdueModel) {
    if (model.hasLoadedOverdueAppointments) {
      ui.showOverdueAppointments(
          model.overdueAppointmentSections!!,
          model.selectedOverdueAppointments,
          model.overdueListSectionStates
      )
      ui.showOverdueCount(model.overdueCount)
      ui.hideProgress()
      renderOverdueListLoadedViews(model)
      renderOverdueListSelectedCount(model.selectedOverdueAppointments)
    } else {
      renderOverdueListLoadingViews()
    }
  }

  private fun renderOverdueListSelectedCount(selectedOverdueAppointments: Set<UUID>) {
    if (selectedOverdueAppointments.isNotEmpty()) {
      ui.showSelectedOverdueAppointmentCount(selectedOverdueAppointments.size)
    } else {
      ui.hideSelectedOverdueAppointmentCount()
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
