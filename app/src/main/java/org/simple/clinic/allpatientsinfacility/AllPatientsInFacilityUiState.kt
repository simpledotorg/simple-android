package org.simple.clinic.allpatientsinfacility

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult
import java.util.UUID

@Parcelize
data class AllPatientsInFacilityUiState(
    val patientsQueried: Boolean,
    val facilityUiState: FacilityUiState? = null,
    val patients: List<PatientSearchResult> = emptyList()
) : Parcelable {
  companion object {
    val FETCHING_PATIENTS = AllPatientsInFacilityUiState(false)
  }

  fun facilityFetched(facility: Facility): AllPatientsInFacilityUiState =
      copy(facilityUiState = FacilityUiState(facility.uuid, facility.name))

  fun noPatients(): AllPatientsInFacilityUiState =
      copy(patients = emptyList(), patientsQueried = true)

  fun hasPatients(patientSearchResults: List<PatientSearchResult>): AllPatientsInFacilityUiState =
      copy(patients = patientSearchResults, patientsQueried = true)
}

@Parcelize
data class FacilityUiState(
    val uuid: UUID,
    val name: String
) : Parcelable
