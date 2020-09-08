package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import org.simple.clinic.mobius.ViewRenderer

class TeleconsultRecordUiRenderer(
    private val ui: TeleconsultRecordUi
) : ViewRenderer<TeleconsultRecordModel> {

  override fun render(model: TeleconsultRecordModel) {
    ui.setTeleconsultationType(model.teleconsultationType)
    ui.setPatientTookMedicines(model.patientTookMedicines)
    ui.setPatientConsented(model.patientConsented)
  }
}
