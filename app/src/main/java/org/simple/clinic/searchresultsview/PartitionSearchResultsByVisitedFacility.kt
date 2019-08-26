package org.simple.clinic.searchresultsview

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.PatientToFacilityId
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.FacilityUuid
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.PatientUuid

class PartitionSearchResultsByVisitedFacility(
    private val bloodPressureDao: BloodPressureMeasurement.RoomDao,
    private val facilityStream: Observable<Facility>
) : ObservableTransformer<List<PatientSearchResult>, PatientSearchResults> {

  override fun apply(upstream: Observable<List<PatientSearchResult>>): ObservableSource<PatientSearchResults> {
    val searchResults = upstream.replay().refCount()

    val patientToFacilityUuidStream = searchResults
        .map { patients -> patients.map { it.uuid } }
        .switchMap {
          bloodPressureDao
              .patientToFacilityIds(it)
              .toObservable()
        }

    return Observables.combineLatest(searchResults, patientToFacilityUuidStream, facilityStream)
        .map { (patients, patientToFacilities, facility) ->
          val patientsToVisitedFacilities = mapPatientsToVisitedFacilities(patientToFacilities)

          val (patientsInCurrentFacility, patientsInOtherFacility) = patients.partition { patientSearchResult ->
            hasPatientVisitedFacility(
                patientsToVisitedFacilities = patientsToVisitedFacilities,
                facilityUuid = facility.uuid,
                patientUuid = patientSearchResult.uuid
            )
          }

          PatientSearchResults(
              visitedCurrentFacility = patientsInCurrentFacility,
              notVisitedCurrentFacility = patientsInOtherFacility,
              currentFacility = facility
          )
        }
  }

  private fun hasPatientVisitedFacility(
      patientsToVisitedFacilities: Map<PatientUuid, Set<FacilityUuid>>,
      facilityUuid: FacilityUuid,
      patientUuid: PatientUuid
  ): Boolean {
    return patientsToVisitedFacilities[patientUuid]?.contains(facilityUuid) ?: false
  }

  private fun mapPatientsToVisitedFacilities(patientToFacilities: List<PatientToFacilityId>): Map<PatientUuid, Set<FacilityUuid>> {
    return patientToFacilities
        .fold(mutableMapOf<PatientUuid, MutableSet<FacilityUuid>>()) { facilityUuids, (patientUuid, facilityUuid) ->
          if (patientUuid !in facilityUuids) {
            facilityUuids[patientUuid] = mutableSetOf()
          }

          facilityUuids[patientUuid]?.add(facilityUuid)
          facilityUuids
        }
  }
}
