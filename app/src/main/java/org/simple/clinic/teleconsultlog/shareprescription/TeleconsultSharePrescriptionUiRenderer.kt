package org.simple.clinic.teleconsultlog.shareprescription

import org.simple.clinic.mobius.ViewRenderer

class TeleconsultSharePrescriptionUiRenderer(
    private val ui: TeleconsultSharePrescriptionUi
) : ViewRenderer<TeleconsultSharePrescriptionModel> {
  override fun render(model: TeleconsultSharePrescriptionModel) {
    renderPatientInformation(model)
    downloadButtonVisibility(model)
  }

  private fun downloadButtonVisibility(model: TeleconsultSharePrescriptionModel) {
    if (model.isPrescriptionDownloading)
      ui.showDownloadProgress()
    else
      ui.hideDownloadProgress()
  }

  private fun renderPatientInformation(model: TeleconsultSharePrescriptionModel) {
    if (model.hasPatientProfile) {
      ui.renderPrescriptionDate(model.prescriptionDate)
      ui.renderPatientInformation(model.patientProfile!!)
    }
    loadPatientMedicines(model)
  }

  private fun loadPatientMedicines(model: TeleconsultSharePrescriptionModel) {
    if (model.hasMedicines)
      ui.renderPatientMedicines(model.medicines!!)
  }
}
