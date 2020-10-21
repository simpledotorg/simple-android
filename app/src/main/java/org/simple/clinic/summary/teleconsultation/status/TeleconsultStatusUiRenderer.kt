package org.simple.clinic.summary.teleconsultation.status

import org.simple.clinic.mobius.ViewRenderer

class TeleconsultStatusUiRenderer(private val ui: TeleconsultStatusUi) : ViewRenderer<TeleconsultStatusModel> {

  override fun render(model: TeleconsultStatusModel) {
    if (model.hasTeleconsultStatus) {
      ui.enableDoneButton()
    }
  }
}
