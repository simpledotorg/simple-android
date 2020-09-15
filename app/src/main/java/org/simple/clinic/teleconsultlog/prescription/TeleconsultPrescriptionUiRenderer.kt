package org.simple.clinic.teleconsultlog.prescription

import org.simple.clinic.mobius.ViewRenderer

class TeleconsultPrescriptionUiRenderer(
    private val ui: TeleconsultPrescriptionUi
) : ViewRenderer<TeleconsultPrescriptionModel> {

  override fun render(model: TeleconsultPrescriptionModel) {
    if (model.hasPatient) {
      ui.renderPatientDetails(model.patient!!)
    }
  }
}
