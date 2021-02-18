package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.mobius.ViewRenderer

class NewMedicalHistoryUiRenderer(
    private val ui: NewMedicalHistoryUi
) : ViewRenderer<NewMedicalHistoryModel> {

  override fun render(model: NewMedicalHistoryModel) {
    if (model.hasLoadedPatientEntry) {
      ui.setPatientName(model.ongoingPatientEntry!!.personalDetails!!.fullName)
    }

    with(model.ongoingMedicalHistoryEntry) {
      ui.renderAnswerForQuestion(HAS_HAD_A_HEART_ATTACK, hasHadHeartAttack)
      ui.renderAnswerForQuestion(HAS_HAD_A_KIDNEY_DISEASE, hasHadKidneyDisease)
      ui.renderAnswerForQuestion(HAS_HAD_A_STROKE, hasHadStroke)

      if (model.hasLoadedCurrentFacility) {
        if (model.facilityDiabetesManagementEnabled) {
          ui.showDiagnosisView()
          ui.hideDiabetesHistorySection()
          ui.renderDiagnosisAnswer(DIAGNOSED_WITH_HYPERTENSION, diagnosedWithHypertension)
          ui.renderDiagnosisAnswer(DIAGNOSED_WITH_DIABETES, hasDiabetes)
        } else {
          ui.hideDiagnosisView()
          ui.showDiabetesHistorySection()
          ui.renderAnswerForQuestion(DIAGNOSED_WITH_DIABETES, hasDiabetes)
        }
      }
    }

    ui.showDiagnosisRequiredError(model.showDiagnosisRequiredError)

    if (model.nextButtonState == ButtonState.SAVING) {
      ui.showNextButtonProgress()
    } else {
      ui.hideNextButtonProgress()
    }
  }
}
