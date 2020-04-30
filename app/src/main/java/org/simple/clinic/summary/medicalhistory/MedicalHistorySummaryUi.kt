package org.simple.clinic.summary.medicalhistory

import org.simple.clinic.medicalhistory.MedicalHistory

interface MedicalHistorySummaryUi {
  fun populateMedicalHistory(medicalHistory: MedicalHistory)
  fun showDiagnosisView()

  // Not yet migrated to Mobius
  fun hideDiagnosisView()
  fun showDiabetesHistorySection()
  fun hideDiabetesHistorySection()
  fun hideDiagnosisError()
}
