package org.simple.clinic.instantsearch

import org.simple.clinic.mobius.ViewRenderer

class InstantSearchUiRenderer(private val ui: InstantSearchUi) : ViewRenderer<InstantSearchModel> {

  override fun render(model: InstantSearchModel) {
    renderSearchProgress(model)
  }

  private fun renderSearchProgress(model: InstantSearchModel) {
    if (model.instantSearchProgressState == InstantSearchProgressState.IN_PROGRESS) {
      ui.showSearchProgress()
    }
  }
}
