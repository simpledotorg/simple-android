package org.simple.clinic.allpatientsinfacility

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult

data class AllPatientsInFacilityUiState(
    val patientsQueried: Boolean,
    val facility: Facility? = null,
    val patients: List<PatientSearchResult> = emptyList()
) {
  companion object {
    val FETCHING_PATIENTS = AllPatientsInFacilityUiState(false)
  }

  fun facilityFetched(facility: Facility): AllPatientsInFacilityUiState =
      copy(facility = facility)

  fun noPatients(): AllPatientsInFacilityUiState =
      copy(patients = emptyList(), patientsQueried = true)

  fun hasPatients(patientSearchResults: List<PatientSearchResult>): AllPatientsInFacilityUiState =
      copy(patients = patientSearchResults, patientsQueried = true)
}
