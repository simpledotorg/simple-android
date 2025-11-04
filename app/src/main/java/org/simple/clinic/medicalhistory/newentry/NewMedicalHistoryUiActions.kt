package org.simple.clinic.medicalhistory.newentry

import java.util.UUID

interface NewMedicalHistoryUiActions {
  fun openPatientSummaryScreen(patientUuid: UUID)
  fun showOngoingHypertensionTreatmentErrorDialog()
  fun showHypertensionDiagnosisRequiredErrorDialog()
  fun showOngoingDiabetesTreatmentErrorDialog()

  fun goBack()
}
