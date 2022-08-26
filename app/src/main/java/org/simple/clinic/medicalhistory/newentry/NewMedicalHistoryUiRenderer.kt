package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithDiabetes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithHypertension
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAHeartAttack
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAKidneyDisease
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAStroke
import org.simple.clinic.mobius.ViewRenderer

class NewMedicalHistoryUiRenderer(
    private val ui: NewMedicalHistoryUi
) : ViewRenderer<NewMedicalHistoryModel> {

  override fun render(model: NewMedicalHistoryModel) {
    if (model.hasLoadedPatientEntry) {
      ui.setPatientName(model.ongoingPatientEntry!!.personalDetails!!.fullName)
    }

    with(model.ongoingMedicalHistoryEntry) {
      ui.renderAnswerForQuestion(HasHadAHeartAttack, hasHadHeartAttack)
      ui.renderAnswerForQuestion(HasHadAKidneyDisease, hasHadKidneyDisease)
      ui.renderAnswerForQuestion(HasHadAStroke, hasHadStroke)
      ui.renderDiagnosisAnswer(DiagnosedWithHypertension, diagnosedWithHypertension)
      renderHypertensionTreatmentQuestion(model)

      if (model.hasLoadedCurrentFacility && model.facilityDiabetesManagementEnabled) {
        ui.showDiabetesDiagnosisView()
        ui.hideDiabetesHistorySection()
        ui.renderDiagnosisAnswer(DiagnosedWithDiabetes, hasDiabetes)
        renderDiabetesTreatmentQuestion(model)
      } else {
        ui.hideDiabetesDiagnosisView()
        ui.showDiabetesHistorySection()
        ui.renderAnswerForQuestion(DiagnosedWithDiabetes, hasDiabetes)
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
