package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
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
      ui.renderDiagnosisAnswer(DIAGNOSED_WITH_HYPERTENSION, diagnosedWithHypertension)
      renderHypertensionTreatmentQuestion(model)

      if (model.hasLoadedCurrentFacility && model.facilityDiabetesManagementEnabled) {
        ui.showDiabetesDiagnosisView()
        ui.hideDiabetesHistorySection()
        ui.renderDiagnosisAnswer(DIAGNOSED_WITH_DIABETES, hasDiabetes)
        renderDiabetesTreatmentQuestion(model)
      } else {
        ui.hideDiabetesDiagnosisView()
        ui.showDiabetesHistorySection()
        ui.renderAnswerForQuestion(DIAGNOSED_WITH_DIABETES, hasDiabetes)
      }
    }

    renderNextButton(model)
  }

  private fun renderDiabetesTreatmentQuestion(model: NewMedicalHistoryModel) {
    if (model.showOngoingDiabetesTreatment) {
      ui.showDiabetesTreatmentQuestion(model.ongoingMedicalHistoryEntry.isOnDiabetesTreatment)
    } else {
      ui.hideDiabetesTreatmentQuestion()
    }
  }

  private fun renderHypertensionTreatmentQuestion(model: NewMedicalHistoryModel) {
    if (model.showOngoingHypertensionTreatment) {
      ui.showHypertensionTreatmentQuestion(model.ongoingMedicalHistoryEntry.isOnHypertensionTreatment)
    } else {
      ui.hideHypertensionTreatmentQuestion()
    }
  }

  private fun renderNextButton(model: NewMedicalHistoryModel) {
    if (model.registeringPatient) {
      ui.showNextButtonProgress()
    } else {
      ui.hideNextButtonProgress()
    }
  }
}
