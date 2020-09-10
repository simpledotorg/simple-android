package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import org.simple.clinic.mobius.ViewRenderer

class TeleconsultRecordUiRenderer(
    private val ui: TeleconsultRecordUi
) : ViewRenderer<TeleconsultRecordModel> {

  override fun render(model: TeleconsultRecordModel) {
    renderPatientDetails(model)
    ui.setTeleconsultationType(model.teleconsultationType)
    ui.setPatientTookMedicines(model.patientTookMedicines)
    ui.setPatientConsented(model.patientConsented)
  }

  private fun renderPatientDetails(model: TeleconsultRecordModel) {
    if (model.hasPatient) {
      ui.renderPatientDetails(model.patient!!)
    }
  }
}
