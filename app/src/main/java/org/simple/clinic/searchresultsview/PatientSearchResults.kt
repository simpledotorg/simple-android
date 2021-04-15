package org.simple.clinic.searchresultsview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.bp.PatientToFacilityId
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult
import java.util.UUID

@Parcelize
data class PatientSearchResults(
    val visitedCurrentFacility: List<PatientSearchResult>,
    val notVisitedCurrentFacility: List<PatientSearchResult>,
    val currentFacility: Facility?
): Parcelable {
  companion object {
    val EMPTY_RESULTS = PatientSearchResults(emptyList(), emptyList(), null)

    fun from(
        searchResults: List<PatientSearchResult>,
        patientToFacilityIds: List<PatientToFacilityId>,
        currentFacility: Facility
    ): PatientSearchResults {
      val patientsToVisitedFacilities = mapPatientsToVisitedFacilities(patientToFacilityIds)

      val (patientsInCurrentFacility, patientsInOtherFacility) = searchResults.partition { patientSearchResult ->
        hasPatientVisitedFacility(
            patientsToVisitedFacilities = patientsToVisitedFacilities,
            facilityUuid = currentFacility.uuid,
            patientUuid = patientSearchResult.uuid
        )
      }

      return PatientSearchResults(
          visitedCurrentFacility = patientsInCurrentFacility,
          notVisitedCurrentFacility = patientsInOtherFacility,
          currentFacility = currentFacility
      )
    }

    private fun mapPatientsToVisitedFacilities(patientToFacilities: List<PatientToFacilityId>): Map<UUID, Set<UUID>> {
      return patientToFacilities
          .fold(mutableMapOf<UUID, MutableSet<UUID>>()) { facilityUuids, (patientUuid, facilityUuid) ->
            if (patientUuid !in facilityUuids) {
              facilityUuids[patientUuid] = mutableSetOf()
            }

            facilityUuids[patientUuid]?.add(facilityUuid)
            facilityUuids
          }
    }

    private fun hasPatientVisitedFacility(
        patientsToVisitedFacilities: Map<UUID, Set<UUID>>,
        facilityUuid: UUID,
        patientUuid: UUID
    ): Boolean {
      return patientsToVisitedFacilities[patientUuid]?.contains(facilityUuid) ?: false
    }
  }

  val hasNoResults: Boolean
    get() = visitedCurrentFacility.isEmpty() && notVisitedCurrentFacility.isEmpty()

}
