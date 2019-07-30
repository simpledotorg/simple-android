package org.simple.clinic.allpatientsinfacility

import org.simple.clinic.patient.PatientSearchResult

interface AllPatientsInFacilityUi {

  fun showNoPatientsFound(facilityName: String)

  fun showPatients(facilityUiState: FacilityUiState, patientSearchResults: List<PatientSearchResult>)
}
