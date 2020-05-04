package org.simple.clinic.summary.medicalhistory

import org.simple.clinic.mobius.ViewRenderer

class MedicalHistorySummaryUiRenderer(
    private val ui: MedicalHistorySummaryUi
) : ViewRenderer<MedicalHistorySummaryModel> {

  override fun render(model: MedicalHistorySummaryModel) {
    if (model.hasLoadedMedicalHistory) {
      ui.populateMedicalHistory(model.medicalHistory!!)
    }
  }
}
