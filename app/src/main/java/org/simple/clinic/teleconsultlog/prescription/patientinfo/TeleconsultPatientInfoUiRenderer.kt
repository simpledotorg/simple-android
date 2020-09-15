package org.simple.clinic.teleconsultlog.prescription.patientinfo

import org.simple.clinic.mobius.ViewRenderer

class TeleconsultPatientInfoUiRenderer(
    private val ui: TeleconsultPatientInfoUi
) : ViewRenderer<TeleconsultPatientInfoModel> {

  override fun render(model: TeleconsultPatientInfoModel) {
    if (model.hasPatientProfile) {
      ui.renderPatientInformation(model.patientProfile!!)
      ui.renderPrescriptionDate(model.prescriptionDate)
    }
  }
}
