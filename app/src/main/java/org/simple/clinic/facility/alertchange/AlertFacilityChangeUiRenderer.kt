package org.simple.clinic.facility.alertchange

import org.simple.clinic.mobius.ViewRenderer

class AlertFacilityChangeUiRenderer(
    private val ui: AlertFacilityChangeUi
) : ViewRenderer<AlertFacilityChangeModel> {

  override fun render(model: AlertFacilityChangeModel) {
    if (model.isFacilityChanged) {
      ui.showFacilityChangeAlert()
    }
  }
}
