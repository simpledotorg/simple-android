package org.simple.clinic.summary

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.summary.teleconsultation.api.TeleconsultInfo

class PatientSummaryViewRenderer(
    private val ui: PatientSummaryScreenUi
) : ViewRenderer<PatientSummaryModel> {

  override fun render(model: PatientSummaryModel) {
    with(ui) {
      if (model.hasLoadedPatientSummaryProfile) {
        populatePatientProfile(model.patientSummaryProfile!!)
        showEditButton()
      }

      if (model.hasLoadedCurrentFacility) {
        setupUiForDiabetesManagement(model.isDiabetesManagementEnabled)
        setupUiForTeleconsult(model)
      }
    }
  }

  private fun setupUiForTeleconsult(model: PatientSummaryModel) {
    if (model.isTeleconsultationEnabled && model.isUserLoggedIn) {
      renderContactDoctorButton(model)
    } else {
      ui.hideContactDoctorButton()
    }
  }

  private fun renderContactDoctorButton(model: PatientSummaryModel) {
    ui.showContactDoctorButton()
    when (model.teleconsultInfo) {
      is TeleconsultInfo.Fetched -> ui.enableContactDoctorButton()
      is TeleconsultInfo.MissingPhoneNumber, is TeleconsultInfo.NetworkError -> ui.disableContactDoctorButton()
      is TeleconsultInfo.Fetching -> ui.fetchingTeleconsultInfo()
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
