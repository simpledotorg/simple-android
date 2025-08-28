package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithDiabetes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithHypertension
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAHeartAttack
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAKidneyDisease
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAStroke
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IsSmoking
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IsUsingSmokelessTobacco
import org.simple.clinic.mobius.ViewRenderer

class NewMedicalHistoryUiRenderer(
    private val ui: NewMedicalHistoryUi
) : ViewRenderer<NewMedicalHistoryModel> {

  override fun render(model: NewMedicalHistoryModel) {
    if (model.hasLoadedPatientEntry) {
      renderPatientName(model)
    }

    renderMedicalHistoryQuestions(model)

    if (model.hasLoadedCurrentFacility && model.facilityDiabetesManagementEnabled) {
      renderDiabetesManagementEnabled(model)
    } else {
      renderDiabetesManagementDisabled(model)
    }

    renderSmokingQuestion(model)
    renderSmokelessTobaccoQuestion(model)

    renderNextButton(model)
  }

  private fun renderPatientName(model: NewMedicalHistoryModel) {
    ui.setPatientName(model.ongoingPatientEntry!!.personalDetails!!.fullName)
  }

  private fun renderMedicalHistoryQuestions(model: NewMedicalHistoryModel) {
    with(model.ongoingMedicalHistoryEntry) {
      ui.renderAnswerForQuestion(HasHadAHeartAttack, hasHadHeartAttack)
      ui.renderAnswerForQuestion(HasHadAKidneyDisease, hasHadKidneyDisease)
      ui.renderAnswerForQuestion(HasHadAStroke, hasHadStroke)
      ui.renderDiagnosisAnswer(DiagnosedWithHypertension, diagnosedWithHypertension)
      renderHypertensionTreatmentQuestion(model)
    }
  }

  private fun renderHypertensionTreatmentQuestion(model: NewMedicalHistoryModel) {
    if (model.showOngoingHypertensionTreatment) {
      ui.showHypertensionTreatmentQuestion(model.ongoingMedicalHistoryEntry.isOnHypertensionTreatment)
    } else {
      ui.hideHypertensionTreatmentQuestion()
    }
  }

  private fun renderDiabetesManagementEnabled(model: NewMedicalHistoryModel) {
    ui.showDiabetesDiagnosisView()
    ui.hideDiabetesHistorySection()
    ui.renderDiagnosisAnswer(DiagnosedWithDiabetes, model.ongoingMedicalHistoryEntry.hasDiabetes)
    renderDiabetesTreatmentQuestion(model)
  }

  private fun renderDiabetesTreatmentQuestion(model: NewMedicalHistoryModel) {
    if (model.showOngoingDiabetesTreatment) {
      ui.showDiabetesTreatmentQuestion(model.ongoingMedicalHistoryEntry.isOnDiabetesTreatment)
    } else {
      ui.hideDiabetesTreatmentQuestion()
    }
  }

  private fun renderDiabetesManagementDisabled(model: NewMedicalHistoryModel) {
    ui.hideDiabetesDiagnosisView()
    ui.showDiabetesHistorySection()
    ui.renderAnswerForQuestion(DiagnosedWithDiabetes, model.ongoingMedicalHistoryEntry.hasDiabetes)
  }

  private fun renderSmokingQuestion(model: NewMedicalHistoryModel) {
    if (model.showIsSmokingQuestion) {
      ui.showCurrentSmokerQuestion()
      ui.renderAnswerForQuestion(IsSmoking, model.ongoingMedicalHistoryEntry.isSmoking)
    } else {
      ui.hideCurrentSmokerQuestion()
    }
  }

  private fun renderSmokelessTobaccoQuestion(model: NewMedicalHistoryModel) {
    if (model.showSmokelessTobaccoQuestion) {
      ui.showSmokelessTobaccoQuestion()
      ui.renderAnswerForQuestion(IsUsingSmokelessTobacco, model.ongoingMedicalHistoryEntry.isUsingSmokelessTobacco)
    } else {
      ui.hideSmokelessTobaccoQuestion()
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
