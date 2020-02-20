package org.simple.clinic.summary

interface PatientSummaryScreenUi : PatientSummaryUiActions {
  fun populatePatientProfile(patientSummaryProfile: PatientSummaryProfile)
  fun showEditButton()
  fun showDiabetesView()
  fun hideDiabetesView()
}
