package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.mobius.ViewRenderer

class NewMedicalHistoryUiRenderer(
    private val ui: NewMedicalHistoryUi
) : ViewRenderer<NewMedicalHistoryModel> {

  override fun render(model: NewMedicalHistoryModel) {
    if(model.hasLoadedPatientEntry) {
      ui.setPatientName(model.ongoingPatientEntry!!.personalDetails!!.fullName)
    }
  }
}
