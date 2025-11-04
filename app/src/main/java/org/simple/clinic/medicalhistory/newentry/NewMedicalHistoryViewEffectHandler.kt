package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class NewMedicalHistoryViewEffectHandler(
    private val uiActions: NewMedicalHistoryUiActions
) : ViewEffectsHandler<NewMedicalHistoryViewEffect> {

  override fun handle(viewEffect: NewMedicalHistoryViewEffect) {
    when (viewEffect) {
      is OpenPatientSummaryScreen -> uiActions.openPatientSummaryScreen(viewEffect.patientUuid)
      ShowOngoingHypertensionTreatmentError -> uiActions.showOngoingHypertensionTreatmentErrorDialog()
      ShowHypertensionDiagnosisRequiredError -> uiActions.showHypertensionDiagnosisRequiredErrorDialog()
      ShowOngoingDiabetesTreatmentErrorDialog -> uiActions.showOngoingDiabetesTreatmentErrorDialog()
      GoBack -> uiActions.goBack()
    }.exhaustive()
  }
}
