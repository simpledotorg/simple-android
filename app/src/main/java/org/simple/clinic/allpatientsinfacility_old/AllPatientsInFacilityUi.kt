package org.simple.clinic.allpatientsinfacility_old

interface AllPatientsInFacilityUi {

  fun showNoPatientsFound(facilityName: String)

  fun showPatients(facilityUiState: FacilityUiState, patientSearchResults: List<PatientSearchResultUiState>)
}
