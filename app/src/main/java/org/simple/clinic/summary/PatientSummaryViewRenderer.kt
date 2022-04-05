package org.simple.clinic.summary

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class PatientSummaryViewRenderer(
    private val ui: PatientSummaryScreenUi,
    private val isNextAppointmentFeatureEnabled: Boolean,
    private val modelUpdateCallback: PatientSummaryModelUpdateCallback
) : ViewRenderer<PatientSummaryModel> {

  private val clinicalDecisionSupportCallback = ValueChangedCallback<Boolean>()

  override fun render(model: PatientSummaryModel) {
    modelUpdateCallback?.invoke(model)

    with(ui) {
      if (model.hasLoadedPatientSummaryProfile) {
        populatePatientProfile(model.patientSummaryProfile!!)
        showEditButton()
        setupUiForAssignedFacility(model)
        renderPatientDiedStatus(model)
      }

      if (model.hasLoadedCurrentFacility) {
        setupUiForDiabetesManagement(model.isDiabetesManagementEnabled)
        setupUiForTeleconsult(model)
      }

      renderNextAppointmentCard(model)
      renderClinicalDecisionSupportAlert(model)
    }
  }

  private fun renderClinicalDecisionSupportAlert(model: PatientSummaryModel) {
    val canShowClinicalDecisionSupportAlert = model.hasPatientRegistrationData == true && model.readyToRender()
    if (!canShowClinicalDecisionSupportAlert) {
      ui.hideClinicalDecisionSupportAlertWithoutAnimation()
      return
    }

    clinicalDecisionSupportCallback.pass(model.isNewestBpEntryHigh == true) { isNewestBpEntryHigh ->
      if (isNewestBpEntryHigh) {
        ui.showClinicalDecisionSupportAlert()
      } else {
        ui.hideClinicalDecisionSupportAlert()
      }
    }
  }

  private fun renderNextAppointmentCard(model: PatientSummaryModel) {
    if (isNextAppointmentFeatureEnabled && model.hasPatientRegistrationData == true) {
      ui.showNextAppointmentCard()
    } else {
      ui.hideNextAppointmentCard()
    }
  }

  private fun renderPatientDiedStatus(model: PatientSummaryModel) {
    if (model.hasPatientDied) {
      ui.showPatientDiedStatus()
    } else {
      ui.hidePatientDiedStatus()
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
