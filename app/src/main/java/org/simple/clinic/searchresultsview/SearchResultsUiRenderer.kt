package org.simple.clinic.searchresultsview

import org.simple.clinic.mobius.ViewRenderer

class SearchResultsUiRenderer(
    private val ui: SearchResultsUi
) : ViewRenderer<SearchResultsModel> {

  override fun render(model: SearchResultsModel) {
    if (model.hasLoadedSearchResults) {
      ui.updateSearchResults(model.patientSearchResults!!)
    }
  }
}
