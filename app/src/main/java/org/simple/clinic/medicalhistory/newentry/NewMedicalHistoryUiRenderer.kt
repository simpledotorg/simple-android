package org.simple.clinic.medicalhistory.newentry

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
      renderDiabetesManagementDisabled()
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
      renderHypertensionTreatmentQuestion(model)
    }
    ui.populateOngoingMedicalHistoryEntry(model.ongoingMedicalHistoryEntry)
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
    renderDiabetesTreatmentQuestion(model)
  }

  private fun renderDiabetesTreatmentQuestion(model: NewMedicalHistoryModel) {
    if (model.showOngoingDiabetesTreatment) {
      ui.showDiabetesTreatmentQuestion(model.ongoingMedicalHistoryEntry.isOnDiabetesTreatment)
    } else {
      ui.hideDiabetesTreatmentQuestion()
    }
  }

  private fun renderDiabetesManagementDisabled() {
    ui.hideDiabetesDiagnosisView()
    ui.showDiabetesHistorySection()
  }

  private fun renderSmokingQuestion(model: NewMedicalHistoryModel) {
    if (model.showIsSmokingQuestion) {
      ui.showCurrentSmokerQuestion()
    } else {
      ui.hideCurrentSmokerQuestion()
    }
  }

  private fun renderSmokelessTobaccoQuestion(model: NewMedicalHistoryModel) {
    if (model.showSmokelessTobaccoQuestion) {
      ui.showSmokelessTobaccoQuestion()
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
