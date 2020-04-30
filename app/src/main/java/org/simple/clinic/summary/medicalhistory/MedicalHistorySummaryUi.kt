package org.simple.clinic.summary.medicalhistory

import org.simple.clinic.medicalhistory.MedicalHistory

interface MedicalHistorySummaryUi: MedicalHistorySummaryUiActions {
  fun populateMedicalHistory(medicalHistory: MedicalHistory)
  fun showDiagnosisView()
  fun hideDiagnosisView()
  fun showDiabetesHistorySection()
  fun hideDiabetesHistorySection()
}
