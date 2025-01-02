package org.simple.clinic.summary.medicalhistory

import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class MedicalHistorySummaryUiRenderer(
    private val ui: MedicalHistorySummaryUi
) : ViewRenderer<MedicalHistorySummaryModel> {

  private val medicalHistoryChangedCallback = ValueChangedCallback<MedicalHistory>()

  private val diabetesManagementFlagChangedCallback = ValueChangedCallback<Boolean>()

  private val smokerQuestionVisibilityChangedCallback = ValueChangedCallback<Boolean>()

  override fun render(model: MedicalHistorySummaryModel) {
    if (model.hasLoadedMedicalHistory) {
      medicalHistoryChangedCallback.pass(model.medicalHistory!!, ui::populateMedicalHistory)
    }

    if (model.hasLoadedCurrentFacility) {
      diabetesManagementFlagChangedCallback.pass(model.currentFacility!!.config.diabetesManagementEnabled, ::toggleDiabetesManagementUi)
    }

    smokerQuestionVisibilityChangedCallback.pass(model.showIsSmokingQuestion) { show ->
      if (show) {
        ui.showCurrentSmokerQuestion()
      } else {
        ui.hideCurrentSmokerQuestion()
      }
    }
  }

  private fun toggleDiabetesManagementUi(diabetesManagementEnabled: Boolean) {
    if (diabetesManagementEnabled) {
      ui.showDiagnosisView()
      ui.hideDiabetesHistorySection()
    } else {
      ui.hideDiagnosisView()
      ui.showDiabetesHistorySection()
    }
  }
}
