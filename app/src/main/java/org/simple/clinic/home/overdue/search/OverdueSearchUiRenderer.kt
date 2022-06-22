package org.simple.clinic.home.overdue.search

import org.simple.clinic.mobius.ViewRenderer

class OverdueSearchUiRenderer(
    private val ui: OverdueSearchUi
) : ViewRenderer<OverdueSearchModel> {

  override fun render(model: OverdueSearchModel) {
    if (!model.hasSearchQuery) {
      ui.showSearchHistory(model.overdueSearchHistory.orEmpty())
      ui.hideSearchResults()
    } else {
      ui.hideSearchHistory()
      showOverdueSearchResults(model)
    }
  }

  private fun showOverdueSearchResults(model: OverdueSearchModel) {
    if (model.hasOverdueSearchResults) {
      ui.showOverdueSearchResults(model.overdueSearchResults!!)
    } else {
      ui.hideSearchResults()
    }
  }
}
