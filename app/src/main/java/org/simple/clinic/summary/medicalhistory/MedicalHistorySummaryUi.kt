package org.simple.clinic.summary.medicalhistory

import org.simple.clinic.medicalhistory.MedicalHistory

interface MedicalHistorySummaryUi {
  fun populateMedicalHistory(medicalHistory: MedicalHistory)
  fun showDiagnosisView()
  fun hideDiagnosisView()
  fun showCurrentSmokerQuestion()
  fun hideCurrentSmokerQuestion()

  fun showSmokelessTobaccoQuestion()
  fun hideSmokelessTobaccoQuestion()
  fun setHypertensionSuspectedOptionVisibility(visible: Boolean)
  fun setDiabetesSuspectedOptionVisibility(visible: Boolean)
}
