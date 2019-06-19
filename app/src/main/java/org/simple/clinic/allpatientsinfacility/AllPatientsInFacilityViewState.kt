package org.simple.clinic.allpatientsinfacility

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult

data class AllPatientsInFacilityViewState(
    val patientsQueried: Boolean,
    val facility: Facility? = null,
    val patients: List<PatientSearchResult> = emptyList()
) {
  companion object {
    val FETCHING_PATIENTS = AllPatientsInFacilityViewState(false)
  }

  fun facilityFetched(facility: Facility): AllPatientsInFacilityViewState =
      copy(facility = facility)

  fun noPatients(): AllPatientsInFacilityViewState =
      copy(patients = emptyList(), patientsQueried = true)

  fun hasPatients(patientSearchResults: List<PatientSearchResult>): AllPatientsInFacilityViewState =
      copy(patients = patientSearchResults, patientsQueried = true)
}
