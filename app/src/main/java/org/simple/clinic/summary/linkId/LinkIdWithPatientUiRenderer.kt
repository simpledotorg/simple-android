package org.simple.clinic.summary.linkId

import org.simple.clinic.mobius.ViewRenderer

class LinkIdWithPatientUiRenderer(private val ui: LinkIdWithPatientViewUi) : ViewRenderer<LinkIdWithPatientModel> {

  override fun render(model: LinkIdWithPatientModel) {
    if (model.hasPatientName) {
      ui.renderPatientName(model.patientName!!)
    }

    if (model.buttonState == ButtonState.SAVING) {
      ui.showAddButtonProgress()
    } else {
      ui.hideAddButtonProgress()
    }
  }
}
