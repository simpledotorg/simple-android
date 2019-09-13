package org.simple.clinic.allpatientsinfacility

interface AllPatientsInFacilityUi {
  fun showNoPatientsFound(facilityName: String)

  fun showPatients(
      facilityUiState: FacilityUiState,
      patientSearchResults: List<PatientSearchResultUiState>
  )
}
