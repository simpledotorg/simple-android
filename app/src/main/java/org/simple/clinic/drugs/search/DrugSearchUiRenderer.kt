package org.simple.clinic.drugs.search

import org.simple.clinic.mobius.ViewRenderer

class DrugSearchUiRenderer(private val ui: DrugSearchUi) : ViewRenderer<DrugSearchModel> {

  override fun render(model: DrugSearchModel) {
    if (!model.hasSearchQuery) {
      ui.hideSearchResults()
    }
  }
}
