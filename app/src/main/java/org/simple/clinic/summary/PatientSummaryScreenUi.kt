package org.simple.clinic.summary

interface PatientSummaryScreenUi {
  fun populatePatientProfile(patientSummaryProfile: PatientSummaryProfile)
  fun showEditButton()
  fun showDiabetesView()
  fun hideDiabetesView()
  fun showTeleconsultButton()
  fun hideTeleconsultButton()
  fun showAssignedFacilityView()
  fun hideAssignedFacilityView()
  fun hideDoneButton()
  fun showTeleconsultLogButton()
}
