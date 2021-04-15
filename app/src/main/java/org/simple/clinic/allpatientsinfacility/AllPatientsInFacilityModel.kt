package org.simple.clinic.allpatientsinfacility

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.PatientSearchResult.LastSeen
import java.time.LocalDate
import java.util.Objects
import java.util.UUID

@Parcelize
data class AllPatientsInFacilityModel(
    val patientsQueried: Boolean,
    val facilityUiState: FacilityUiState? = null
) : Parcelable {
  companion object {
    val FETCHING_PATIENTS = AllPatientsInFacilityModel(false)
  }

  @IgnoredOnParcel
  var patients: List<PatientSearchResultUiState> = emptyList()

  fun facilityFetched(facility: Facility): AllPatientsInFacilityModel =
      copy(facilityUiState = FacilityUiState(facility.uuid, facility.name))

  fun noPatients(): AllPatientsInFacilityModel =
      copy(patientsQueried = true).apply { patients = emptyList() }

  fun hasPatients(patientSearchResults: List<PatientSearchResultUiState>): AllPatientsInFacilityModel =
      copy(patientsQueried = true).apply { patients = patientSearchResults }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as AllPatientsInFacilityModel

    if (patientsQueried != other.patientsQueried) return false
    if (facilityUiState != other.facilityUiState) return false
    if (patients != other.patients) return false

    return true
  }

  override fun hashCode(): Int {
    return Objects.hash(patientsQueried, facilityUiState, patients)
  }

  override fun toString(): String {
    return "AllPatientsInFacilityModel(patientsQueried=$patientsQueried, facilityUiState=$facilityUiState, patients=$patients)"
  }
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
    val lastSeen: LastSeen?
) : Parcelable {

  constructor(searchResult: PatientSearchResult) : this(
      searchResult.uuid,
      searchResult.fullName,
      searchResult.gender,
      searchResult.age,
      searchResult.dateOfBirth,
      searchResult.address,
      searchResult.phoneNumber,
      searchResult.lastSeen
  )
}
