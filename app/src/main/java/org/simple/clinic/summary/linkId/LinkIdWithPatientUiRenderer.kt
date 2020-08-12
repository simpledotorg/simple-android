package org.simple.clinic.summary.linkId

import org.simple.clinic.mobius.ViewRenderer

class LinkIdWithPatientUiRenderer(private val ui: LinkIdWithPatientViewUi) : ViewRenderer<LinkIdWithPatientModel> {

  override fun render(model: LinkIdWithPatientModel) {
    if (model.hasIdentifier) {
      ui.renderIdentifierText(model.identifier!!)
    }
  }
}
