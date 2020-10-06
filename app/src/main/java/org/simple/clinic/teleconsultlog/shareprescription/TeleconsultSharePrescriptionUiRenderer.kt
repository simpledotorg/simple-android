package org.simple.clinic.teleconsultlog.shareprescription

import org.simple.clinic.mobius.ViewRenderer

class TeleconsultSharePrescriptionUiRenderer(
    private val ui: TeleconsultSharePrescriptionUi
) : ViewRenderer<TeleconsultSharePrescriptionModel> {
  override fun render(model: TeleconsultSharePrescriptionModel) {
    if (model.hasPatient) {
      ui.renderPatientDetails(model.patient!!)
      ui.renderPrescriptionDate(model.prescriptionDate)
      if (model.hasMedicines)
        ui.renderPatientMedicines(model.medicines!!)
    }
    if (model.hasPatientProfile)
      ui.renderPatientInformation(model.patientProfile!!)
  }
}
