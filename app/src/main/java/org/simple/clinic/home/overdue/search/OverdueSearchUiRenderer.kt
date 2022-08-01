package org.simple.clinic.home.overdue.search

import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.DONE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.IN_PROGRESS
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.NO_RESULTS
import org.simple.clinic.mobius.ViewRenderer
import java.util.UUID

class OverdueSearchUiRenderer(
    private val ui: OverdueSearchUi,
    private val isOverdueSearchV2Enabled: Boolean
) : ViewRenderer<OverdueSearchModel> {

  override fun render(model: OverdueSearchModel) {
    ui.hideProgress()

    if (model.hasVillageAndPatientNames) {
      ui.setOverdueSearchSuggestions(model.villageAndPatientNames!!)
    }

    if (model.hasSearchQuery || model.hasSearchInputs) {
      renderSearchResults(model)
    } else {
      ui.hideNoSearchResults()
      ui.hideSearchResults()
      ui.hideDownloadAndShareButtons()
      renderSearchHistory(model)
    }
  }

  private fun renderSearchHistory(model: OverdueSearchModel) {
    if (!isOverdueSearchV2Enabled) {
      ui.showSearchHistory(model.overdueSearchHistory.orEmpty())
    } else {
      ui.hideSearchHistory()
    }
  }

  private fun renderSearchResults(model: OverdueSearchModel) {
    when (model.overdueSearchProgressState) {
      IN_PROGRESS -> ui.showProgress()
      NO_RESULTS -> renderNoResults()
      DONE -> {
        renderResults()
        renderOverdueListSelectedCount(model.selectedOverdueAppointments)
      }
      null -> {
        // No-op
      }
    }
  }

  private fun renderOverdueListSelectedCount(selectedOverdueAppointments: Set<UUID>) {
    if (selectedOverdueAppointments.isNotEmpty()) {
      ui.showSelectedOverdueAppointmentCount(selectedOverdueAppointments.size)
    } else {
      ui.hideSelectedOverdueAppointmentCount()
    }
  }

  private fun renderResults() {
    ui.showSearchResults()
    ui.hideSearchHistory()
    ui.hideNoSearchResults()
    ui.showDownloadAndShareButtons()
  }

  private fun renderNoResults() {
    ui.showNoSearchResults()
    ui.hideSearchResults()
    ui.hideSearchHistory()
    ui.hideDownloadAndShareButtons()
  }
}
