package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion

interface NewMedicalHistoryUi {
  fun setPatientName(patientName: String)
  fun renderAnswerForQuestion(question: MedicalHistoryQuestion, answer: Answer)
  fun renderDiagnosisAnswer(question: MedicalHistoryQuestion, answer: Answer)
  fun showDiabetesDiagnosisView()
  fun hideDiabetesDiagnosisView()
  fun showDiabetesHistorySection()
  fun hideDiabetesHistorySection()
  fun showNextButtonProgress()
  fun hideNextButtonProgress()
  fun showHypertensionTreatmentQuestion(answer: Answer)
  fun hideHypertensionTreatmentQuestion()
  fun showDiabetesTreatmentQuestion(answer: Answer)
  fun hideDiabetesTreatmentQuestion()
}
