package org.simple.clinic.searchresultsview

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult

class PartitionSearchResultsByVisitedFacility(
    private val bloodPressureDao: BloodPressureMeasurement.RoomDao,
    private val facilityStream: Observable<Facility>
) : ObservableTransformer<List<PatientSearchResult>, PatientSearchResults> {

  override fun apply(upstream: Observable<List<PatientSearchResult>>): ObservableSource<PatientSearchResults> {
    val searchResults = upstream.replay().refCount()

    val patientToFacilityUuidStream = searchResults
        .map { patients -> patients.map { it.uuid } }
        .map(bloodPressureDao::patientToFacilityIds)

    return Observables.combineLatest(searchResults, patientToFacilityUuidStream, facilityStream)
        .map { (patients, patientToFacilities, facility) -> PatientSearchResults.from(patients, patientToFacilities, facility) }
  }
}
