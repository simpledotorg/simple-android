package org.simple.clinic.summary

interface PatientSummaryScreenUi {
  fun populatePatientProfile(patientSummaryProfile: PatientSummaryProfile)
  fun showEditButton()
  fun showDiabetesView()
  fun hideDiabetesView()
  fun showContactDoctorButton()
  fun hideContactDoctorButton()
  fun showAssignedFacilityView()
  fun hideAssignedFacilityView()
  fun hideDoneButton()
  fun showTeleconsultLogButton()
}
