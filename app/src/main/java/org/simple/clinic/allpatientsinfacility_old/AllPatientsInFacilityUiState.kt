package org.simple.clinic.allpatientsinfacility_old

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.PatientSearchResult.LastBp
import org.threeten.bp.LocalDate
import java.util.UUID

@Parcelize
data class AllPatientsInFacilityUiState(
    val patientsQueried: Boolean,
    val facilityUiState: FacilityUiState? = null,
    val patients: List<PatientSearchResultUiState> = emptyList()
) : Parcelable {
  companion object {
    val FETCHING_PATIENTS = AllPatientsInFacilityUiState(false)
  }

  fun facilityFetched(facility: Facility): AllPatientsInFacilityUiState =
      copy(facilityUiState = FacilityUiState(facility.uuid, facility.name))

  fun noPatients(): AllPatientsInFacilityUiState =
      copy(patients = emptyList(), patientsQueried = true)

  fun hasPatients(patientSearchResults: List<PatientSearchResultUiState>): AllPatientsInFacilityUiState =
      copy(patients = patientSearchResults, patientsQueried = true)
}

@Parcelize
data class FacilityUiState(
    val uuid: UUID,
    val name: String
) : Parcelable

@Parcelize
data class PatientSearchResultUiState(
    val uuid: UUID,
    val fullName: String,
    val gender: Gender,
    val age: Age?,
    val dateOfBirth: LocalDate?,
    val address: PatientAddress,
    val phoneNumber: String?,
    val lastBp: LastBp?
) : Parcelable {

  constructor(searchResult: PatientSearchResult) : this(
      searchResult.uuid,
      searchResult.fullName,
      searchResult.gender,
      searchResult.age,
      searchResult.dateOfBirth,
      searchResult.address,
      searchResult.phoneNumber,
      searchResult.lastBp
  )
}
