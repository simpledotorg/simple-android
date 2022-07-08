package org.simple.clinic.home.overdue.search

import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.DONE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.IN_PROGRESS
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.NO_RESULTS
import org.simple.clinic.mobius.ViewRenderer

class OverdueSearchUiRenderer(
    private val ui: OverdueSearchUi
) : ViewRenderer<OverdueSearchModel> {

  override fun render(model: OverdueSearchModel) {
    ui.hideProgress()

    if (model.hasSearchQuery) {
      renderSearchResults(model)
    } else {
      ui.hideNoSearchResults()
      ui.hideSearchResults()
      ui.showSearchHistory(model.overdueSearchHistory.orEmpty())
    }
  }

  private fun renderSearchResults(model: OverdueSearchModel) {
    when (model.overdueSearchProgressState) {
      IN_PROGRESS -> ui.showProgress()
      NO_RESULTS -> renderNoResults()
      DONE -> renderResults()
      null -> {
        // No-op
      }
    }

    ui.setOverdueSearchResultsPagingData(model.overdueSearchResults, model.selectedOverdueAppointments, model.searchQuery.orEmpty())
  }

  private fun renderResults() {
    ui.showSearchResults()
    ui.hideSearchHistory()
    ui.hideNoSearchResults()
  }

  private fun renderNoResults() {
    ui.showNoSearchResults()
    ui.hideSearchResults()
    ui.hideSearchHistory()
  }
}
