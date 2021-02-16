package org.simple.clinic.summary

import org.simple.clinic.mobius.ViewRenderer

class PatientSummaryViewRenderer(
    private val ui: PatientSummaryScreenUi,
    private val modelUpdateCallback: PatientSummaryModelUpdateCallback
) : ViewRenderer<PatientSummaryModel> {

  override fun render(model: PatientSummaryModel) {
    modelUpdateCallback?.invoke(model)

    with(ui) {
      if (model.hasLoadedPatientSummaryProfile) {
        populatePatientProfile(model.patientSummaryProfile!!)
        showEditButton()
        setupUiForAssignedFacility(model)
      }

      if (model.hasLoadedCurrentFacility) {
        setupUiForDiabetesManagement(model.isDiabetesManagementEnabled)
        setupUiForTeleconsult(model)
      }
    }
  }

  private fun setupUiForAssignedFacility(model: PatientSummaryModel) {
    if (model.hasAssignedFacility) {
      ui.showAssignedFacilityView()
    } else {
      ui.hideAssignedFacilityView()
    }
  }

  private fun setupUiForTeleconsult(model: PatientSummaryModel) {
    if (model.openIntention is OpenIntention.ViewExistingPatientWithTeleconsultLog) {
      renderMedicalOfficerView()
    } else {
      renderUserView(model)
    }
  }

  private fun renderMedicalOfficerView() {
    ui.hideTeleconsultButton()
    ui.hideDoneButton()
    ui.showTeleconsultLogButton()
  }

  private fun renderUserView(model: PatientSummaryModel) {
    if (model.isTeleconsultationEnabled && model.isUserLoggedIn) {
      renderTeleconsultButton(model)
    } else {
      ui.hideTeleconsultButton()
    }
  }

  private fun renderTeleconsultButton(model: PatientSummaryModel) {
    if (model.hasMedicalOfficers) {
      ui.showTeleconsultButton()
    } else {
      ui.hideTeleconsultButton()
    }
  }

  private fun setupUiForDiabetesManagement(isDiabetesManagementEnabled: Boolean) {
    if (isDiabetesManagementEnabled) {
      ui.showDiabetesView()
    } else {
      ui.hideDiabetesView()
    }
  }
}
