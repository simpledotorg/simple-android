package org.simple.clinic.searchresultsview

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = SearchResultsUi
typealias UiChange = (Ui) -> Unit

class PatientSearchViewController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val bloodPressureDao: BloodPressureMeasurement.RoomDao
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return populateSearchResults(replayedEvents)
  }

  private fun populateSearchResults(events: Observable<UiEvent>): Observable<UiChange> {
    val searchResultsStream = events
        .ofType<SearchPatientWithCriteria>()
        .map { it.criteria }
        .flatMap(patientRepository::search)

    val currentFacilityStream = userSession
        .requireLoggedInUser()
        .switchMap(facilityRepository::currentFacility)
        .replay()
        .refCount()

    return searchResultsStream
        .compose(PartitionSearchResultsByVisitedFacility(bloodPressureDao, currentFacilityStream))
        .map { results ->
          { ui: Ui ->
            ui.updateSearchResults(results)
          }
        }
  }
}
