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

  private val smokelessTobaccoQuestionVisibilityChangedCallback = ValueChangedCallback<Boolean>()

  private val hypertensionSuspectedVisibilityChangedCallback =
      ValueChangedCallback<Boolean>()

  private val diabetesSuspectedVisibilityChangedCallback =
      ValueChangedCallback<Boolean>()


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

    smokelessTobaccoQuestionVisibilityChangedCallback.pass(model.showSmokelessTobaccoQuestion) { show ->
      if (show) {
        ui.showSmokelessTobaccoQuestion()
      } else {
        ui.hideSmokelessTobaccoQuestion()
      }
    }

    hypertensionSuspectedVisibilityChangedCallback.pass(
        model.showHypertensionSuspectedOption,
        ui::setHypertensionSuspectedOptionVisibility
    )

    diabetesSuspectedVisibilityChangedCallback.pass(
        model.showDiabetesSuspectedOption,
        ui::setDiabetesSuspectedOptionVisibility
    )
  }

  private fun toggleDiabetesManagementUi(diabetesManagementEnabled: Boolean) {
    if (diabetesManagementEnabled) {
      ui.showDiagnosisView()
    } else {
      ui.hideDiagnosisView()
    }
  }
}
