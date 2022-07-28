package org.simple.clinic.home.overdue.search

import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.DONE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.IN_PROGRESS
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.NO_RESULTS
import org.simple.clinic.mobius.ViewRenderer
import java.util.UUID

class OverdueSearchUiRenderer(
    private val ui: OverdueSearchUi
) : ViewRenderer<OverdueSearchModel> {

  override fun render(model: OverdueSearchModel) {
    ui.hideProgress()

    if (model.hasVillageAndPatientNames) {
      ui.setOverdueSearchSuggestions(model.villageAndPatientNames!!)
    }

    if (model.hasSearchQuery) {
      renderSearchResults(model)
    } else {
      ui.hideNoSearchResults()
      ui.hideSearchResults()
      ui.hideDownloadAndShareButtons()
      ui.showSearchHistory(model.overdueSearchHistory.orEmpty())
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

    ui.setOverdueSearchResultsPagingData(model.overdueSearchResults, model.selectedOverdueAppointments, model.searchQuery.orEmpty())
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
