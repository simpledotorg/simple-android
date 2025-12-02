package org.simple.clinic.medicalhistory.newentry

import java.util.UUID

interface NewMedicalHistoryUiActions {
  fun openPatientSummaryScreen(patientUuid: UUID)
  fun showOngoingHypertensionTreatmentErrorDialog()
  fun showDiagnosisRequiredErrorDialog()

  fun showDiagnosisOrReferralRequiredErrorDialog()
  fun showHypertensionDiagnosisRequiredErrorDialog()
  fun showHypertensionDiagnosisRequiredOrReferralErrorDialog()
  fun showChangeDiagnosisErrorDialog()
  fun showOngoingDiabetesTreatmentErrorDialog()

  fun goBack()
}
