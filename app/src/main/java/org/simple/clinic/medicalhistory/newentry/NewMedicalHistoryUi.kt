package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion

interface NewMedicalHistoryUi {
  fun setPatientName(patientName: String)
  fun renderAnswerForQuestion(question: MedicalHistoryQuestion, answer: Answer)
  fun renderDiagnosisAnswer(question: MedicalHistoryQuestion, answer: Answer)
  fun showDiagnosisRequiredError(showError: Boolean)
  fun showDiagnosisView()
  fun hideDiagnosisView()
  fun showDiabetesHistorySection()
  fun hideDiabetesHistorySection()
  fun showNextButtonProgress()
}
