package org.simple.clinic.summary.medicalhistory

import org.simple.clinic.medicalhistory.MedicalHistory

interface MedicalHistorySummaryUi {
  fun populateMedicalHistory(medicalHistory: MedicalHistory)
  fun showDiagnosisView()
  fun hideDiagnosisView()
  fun showDiabetesHistorySection()
  fun hideDiabetesHistorySection()

  // Not yet migrated to Mobius
  fun hideDiagnosisError()
}
