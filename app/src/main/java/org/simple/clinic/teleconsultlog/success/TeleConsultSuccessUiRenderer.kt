package org.simple.clinic.teleconsultlog.success

import org.simple.clinic.mobius.ViewRenderer

class TeleConsultSuccessUiRenderer(
    private val ui: TeleConsultSuccessUi
) : ViewRenderer<TeleConsultSuccessModel> {
  override fun render(model: TeleConsultSuccessModel) {
    if (model.hasPatient)
      ui.showPatientInfo(model.patient!!)
  }
}
