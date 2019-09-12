package org.simple.clinic.allpatientsinfacility

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
data class AllPatientsInFacilityModel(
    val patientsQueried: Boolean,
    val facilityUiState: FacilityUiState? = null,
    val patients: List<PatientSearchResultUiState> = emptyList()
) : Parcelable {
  companion object {
    val FETCHING_PATIENTS = AllPatientsInFacilityModel(false)
  }

  fun facilityFetched(facility: Facility): AllPatientsInFacilityModel =
      copy(facilityUiState = FacilityUiState(facility.uuid, facility.name))

  fun noPatients(): AllPatientsInFacilityModel =
      copy(patients = emptyList(), patientsQueried = true)

  fun hasPatients(patientSearchResults: List<PatientSearchResultUiState>): AllPatientsInFacilityModel =
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
