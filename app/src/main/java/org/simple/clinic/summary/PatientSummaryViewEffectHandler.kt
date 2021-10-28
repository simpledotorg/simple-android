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
      is ShowScheduleAppointmentSheet -> uiActions.showScheduleAppointmentSheet(viewEffect.patientUuid,
          viewEffect.sheetOpenedFrom,
          viewEffect.currentFacility)
      ShowDiagnosisError -> uiActions.showDiagnosisError()
      is OpenContactPatientScreen -> uiActions.openPatientContactSheet(viewEffect.patientUuid)
      is NavigateToTeleconsultRecordScreen -> uiActions.navigateToTeleconsultRecordScreen(viewEffect.patientUuid, viewEffect.teleconsultRecordId)
      is OpenContactDoctorSheet -> uiActions.openContactDoctorSheet(viewEffect.patientUuid)
    }.exhaustive()
  }
}
