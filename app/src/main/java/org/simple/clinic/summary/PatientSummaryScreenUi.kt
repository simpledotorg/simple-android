package org.simple.clinic.summary

interface PatientSummaryScreenUi {
  fun populatePatientProfile(patientSummaryProfile: PatientSummaryProfile)
  fun showEditButton()
  fun showDiabetesView()
  fun hideDiabetesView()
  fun showContactDoctorButton()
  fun hideContactDoctorButton()
  fun enableContactDoctorButton()
  fun disableContactDoctorButton()
  fun fetchingTeleconsultInfo()
  fun showAssignedFacilityView()
  fun hideAssignedFacilityView()
  fun hideDoneButton()
  fun showTeleconsultLogButton()
}
