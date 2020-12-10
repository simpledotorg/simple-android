package org.simple.clinic.scanid

import org.simple.clinic.mobius.ViewRenderer

class ScanSimpleIdUiRenderer(private val ui: ScanSimpleIdUi) : ViewRenderer<ScanSimpleIdModel> {

  override fun render(model: ScanSimpleIdModel) {
    if (model.isSearching)
      ui.showSearchingForPatient()
    else
      ui.hideSearchingForPatient()
  }
}
