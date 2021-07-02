package org.simple.clinic.instantsearch

import org.simple.clinic.instantsearch.InstantSearchProgressState.*
import org.simple.clinic.mobius.ViewRenderer

class InstantSearchUiRenderer(
    private val ui: InstantSearchUi
) : ViewRenderer<InstantSearchModel> {

  override fun render(model: InstantSearchModel) {
    ui.hideProgress()

    when (model.instantSearchProgressState) {
      IN_PROGRESS -> ui.showProgress()
      DONE -> renderResults()
      NO_RESULTS -> renderNoResults(model)
      null -> {
        // No-op
      }
    }
  }

  private fun renderNoResults(model: InstantSearchModel) {
    ui.hideResults()

    if (model.hasSearchQuery) {
      ui.hideNoPatientsInFacility()
      ui.showNoSearchResults()
    } else {
      ui.hideNoSearchResults()
      ui.showNoPatientsInFacility(model.facility!!.name)
    }
  }

  private fun renderResults() {
    ui.showResults()
    ui.hideNoSearchResults()
    ui.hideNoPatientsInFacility()
  }
}
