package org.simple.clinic.summary.medicalhistory

import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class MedicalHistorySummaryUiRenderer(
    private val ui: MedicalHistorySummaryUi
) : ViewRenderer<MedicalHistorySummaryModel> {

  private val medicalHistoryChangedCallback = ValueChangedCallback<MedicalHistory>()

  override fun render(model: MedicalHistorySummaryModel) {
    if (model.hasLoadedMedicalHistory) {
      medicalHistoryChangedCallback.pass(model.medicalHistory!!, ui::populateMedicalHistory)
    }
  }
}
