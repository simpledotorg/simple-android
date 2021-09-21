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
      ShowDiagnosisRequiredError -> uiActions.showDiagnosisRequiredErrorDialog()
      ShowHypertensionDiagnosisRequiredError -> uiActions.showHypertensionDiagnosisRequiredErrorDialog()
      ShowChangeDiagnosisErrorDialog -> uiActions.showChangeDiagnosisErrorDialog()
      ShowOngoingDiabetesTreatmentErrorDialog -> uiActions.showOngoingDiabetesTreatmentErrorDialog()
    }.exhaustive()
  }
}
