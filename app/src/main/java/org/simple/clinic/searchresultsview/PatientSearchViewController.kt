package org.simple.clinic.searchresultsview

import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.searchresultsview.SearchResultsItemType.InCurrentFacilityHeader
import org.simple.clinic.searchresultsview.SearchResultsItemType.NoPatientsInCurrentFacility
import org.simple.clinic.searchresultsview.SearchResultsItemType.NotInCurrentFacilityHeader
import org.simple.clinic.searchresultsview.SearchResultsItemType.SearchResultRow
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PatientSearchView
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

    return Observable.mergeArray(
        populateSearchResultsFromName(replayedEvents),
        populateSearchResultsFromPhoneNumber(replayedEvents),
        openPatientSummary(replayedEvents),
        createNewPatient(replayedEvents))
  }

  private fun populateSearchResultsFromName(events: Observable<UiEvent>): Observable<UiChange> {
    val searchByPatientNameStream = events
        .ofType<SearchPatientCriteria>()
        .map { it.searchPatientBy }
        .ofType<SearchPatientBy.Name>()
        .map { it.searchText }

    val viewCreated = events.ofType<SearchResultsViewCreated>()

    val currentFacilityStream = userSession
        .requireLoggedInUser()
        .switchMap(facilityRepository::currentFacility)
        .replay()
        .refCount()

    return Observables.combineLatest(viewCreated, searchByPatientNameStream) { _, patientName -> patientName}
        .flatMap(patientRepository::search)
        .compose(PartitionSearchResultsByVisitedFacility(bloodPressureDao, currentFacilityStream))
        .withLatestFrom(currentFacilityStream)
        .map { (results, currentFacility) ->
          { ui: Ui ->
            ui.updateSearchResults(generateListItems(results, currentFacility))
            ui.setEmptyStateVisible(results.visitedCurrentFacility.isEmpty() && results.notVisitedCurrentFacility.isEmpty())
          }
        }
  }

  private fun populateSearchResultsFromPhoneNumber(events: Observable<UiEvent>): Observable<UiChange> {
    val searchByPhoneNumberStream = events
        .ofType<SearchPatientCriteria>()
        .map { it.searchPatientBy }
        .ofType<SearchPatientBy.PhoneNumber>()
        .map { it.searchText }

    val viewCreated = events.ofType<SearchResultsViewCreated>()

    val currentFacilityStream = userSession
        .requireLoggedInUser()
        .switchMap(facilityRepository::currentFacility)
        .replay()
        .refCount()

    return Observables.combineLatest(viewCreated, searchByPhoneNumberStream) { _, phoneNumber -> phoneNumber}
        .flatMap(patientRepository::searchByPhoneNumber)
        .compose(PartitionSearchResultsByVisitedFacility(bloodPressureDao, currentFacilityStream))
        .withLatestFrom(currentFacilityStream)
        .map { (results, currentFacility) ->
          { ui: Ui ->
            ui.updateSearchResults(generateListItems(results, currentFacility))
            ui.setEmptyStateVisible(results.visitedCurrentFacility.isEmpty() && results.notVisitedCurrentFacility.isEmpty())
          }
        }
  }

  private fun generateListItems(
      results: PatientSearchResults,
      currentFacility: Facility
  ): List<SearchResultsItemType<out ViewHolder>> {
    if (results.visitedCurrentFacility.isEmpty() && results.notVisitedCurrentFacility.isEmpty()) return emptyList()

    val itemsInCurrentFacility = if (results.visitedCurrentFacility.isNotEmpty()) {
      results.visitedCurrentFacility.map {
        SearchResultRow(it, currentFacility)
      }
    } else {
      listOf(NoPatientsInCurrentFacility)
    }

    val itemsInOtherFacility = if (results.notVisitedCurrentFacility.isNotEmpty()) {
      listOf(NotInCurrentFacilityHeader) +
          results.notVisitedCurrentFacility.map {
            SearchResultRow(it, currentFacility)
          }
    } else {
      emptyList()
    }
    return listOf(InCurrentFacilityHeader(facilityName = currentFacility.name)) +
        itemsInCurrentFacility +
        itemsInOtherFacility
  }

  private fun openPatientSummary(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<SearchResultClicked>()
        .map { { ui: Ui -> ui.searchResultClicked(it) } }
  }

  private fun createNewPatient(events: Observable<UiEvent>): Observable<UiChange> {
    val searchPatientByStream = events
        .ofType<SearchPatientCriteria>()
        .map { it.searchPatientBy }

    return events.ofType<RegisterNewPatientClicked>()
        .withLatestFrom(searchPatientByStream)
        .map { (_, searchPatientBy) ->
          { ui: Ui -> ui.registerNewPatient(RegisterNewPatient(searchPatientBy)) }
        }
  }
}
