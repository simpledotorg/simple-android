package org.simple.clinic.summary

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class PatientSummaryViewEffectHandler(
    private val uiActions: PatientSummaryUiActions
) : ViewEffectsHandler<PatientSummaryViewEffect> {

  override fun handle(viewEffect: PatientSummaryViewEffect) {
    when (viewEffect) {
      is HandleEditClick -> uiActions.showEditPatientScreen(viewEffect.patientSummaryProfile, viewEffect.currentFacility)
      GoBackToPreviousScreen -> uiActions.goToPreviousScreen()
      GoToHomeScreen -> uiActions.goToHomeScreen()
      is ShowAddPhonePopup -> uiActions.showAddPhoneDialog(viewEffect.patientUuid)
      is ShowLinkIdWithPatientView -> uiActions.showLinkIdWithPatientView(viewEffect.patientUuid, viewEffect.identifier)
      is ShowScheduleAppointmentSheet -> uiActions.showScheduleAppointmentSheet(
          patientUuid = viewEffect.patientUuid,
          sheetOpenedFrom = viewEffect.sheetOpenedFrom,
          currentFacility = viewEffect.currentFacility
      )

      ShowDiagnosisError -> uiActions.showDiagnosisError()
      is OpenContactPatientScreen -> uiActions.openPatientContactSheet(viewEffect.patientUuid)
      is NavigateToTeleconsultRecordScreen -> uiActions.navigateToTeleconsultRecordScreen(viewEffect.patientUuid, viewEffect.teleconsultRecordId)
      is OpenContactDoctorSheet -> uiActions.openContactDoctorSheet(viewEffect.patientUuid)
      ShowAddMeasurementsWarningDialog -> uiActions.showAddMeasurementsWarningDialog()
      ShowAddBloodPressureWarningDialog -> uiActions.showAddBloodPressureWarningDialog()
      ShowAddBloodSugarWarningDialog -> uiActions.showAddBloodSugarWarningDialog()
      OpenSelectFacilitySheet -> uiActions.openSelectFacilitySheet()
      is DispatchNewAssignedFacility -> uiActions.dispatchNewAssignedFacility(viewEffect.facility)
      is ShowUpdatePhonePopup -> uiActions.showUpdatePhoneDialog(viewEffect.patientUuid)
      RefreshNextAppointment -> uiActions.refreshNextAppointment()
      is ShowReassignPatientWarningSheet -> uiActions.showReassignPatientWarningSheet(
          patientUuid = viewEffect.patientUuid,
          currentFacility = viewEffect.currentFacility,
          sheetOpenedFrom = viewEffect.sheetOpenedFrom
      )
      ShowDiabetesDiagnosisWarning -> uiActions.showDiabetesDiagnosisWarning()
      is ShowHypertensionDiagnosisWarning -> uiActions.showHypertensionDiagnosisWarning(viewEffect.continueToDiabetesDiagnosisWarning)
      is ShowTobaccoStatusDialog -> uiActions.showTobaccoStatusDialog()
      is OpenBMIEntrySheet -> uiActions.openBMIEntrySheet(viewEffect.patientUuid)
      is OpenCholesterolEntrySheet -> uiActions.openCholesterolEntrySheet(viewEffect.patientUuid)
    }.exhaustive()
  }
}
