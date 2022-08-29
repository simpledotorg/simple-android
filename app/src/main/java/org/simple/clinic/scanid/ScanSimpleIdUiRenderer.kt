package org.simple.clinic.scanid

import org.simple.clinic.mobius.ViewRenderer

class ScanSimpleIdUiRenderer(private val ui: ScanSimpleIdUi) : ViewRenderer<ScanSimpleIdModel> {

  override fun render(model: ScanSimpleIdModel) {
    if (model.isSearching)
      ui.showSearchingForPatient()
    else
      ui.hideSearchingForPatient()

    renderScanQrError(model.scanErrorState)

    if (model.isOpenedFromEditPatientScreen)
      ui.hideEnteredCodeContainerView()

    ui.setToolBarTitle(model.openedFrom)
  }

  private fun renderScanQrError(scanErrorState: ScanErrorState?) {
    when (scanErrorState) {
      ScanErrorState.InvalidQrCode -> ui.showScanError()
      else -> ui.hideScanError()
    }
  }
}
