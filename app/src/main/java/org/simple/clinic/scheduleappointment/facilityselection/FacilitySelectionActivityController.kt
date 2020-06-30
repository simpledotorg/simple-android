package org.simple.clinic.scheduleappointment.facilityselection

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.change.FacilityChangeConfig
import org.simple.clinic.facility.change.FacilityListItemBuilder
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.location.ScreenLocationUpdates
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = FacilitySelectionActivity
typealias UiChange = (Ui) -> Unit

class FacilitySelectionActivityController @Inject constructor(
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession,
    private val configProvider: Observable<FacilityChangeConfig>,
    private val listItemBuilder: FacilityListItemBuilder,
    private val screenLocationUpdates: ScreenLocationUpdates
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(locationUpdates())
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        showProgressForReadingLocation(replayedEvents),
        showFacilities(replayedEvents),
        toggleSearchFieldInToolbar(replayedEvents),
        changeFacilityAndExit(replayedEvents))
  }

  private fun locationUpdates() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val locationUpdates = Observables
        .combineLatest(events.ofType<ScreenCreated>(), configProvider) { _, config -> config }
        .switchMap { config ->
          screenLocationUpdates.streamUserLocation(
              updateInterval = config.locationUpdateInterval,
              timeout = config.locationListenerExpiry,
              discardOlderThan = config.staleLocationThreshold
          )
        }
        .take(1)
        .map { FacilitySelectionUserLocationUpdated(it) }

    events.mergeWith(locationUpdates)
  }

  private fun showProgressForReadingLocation(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreations = events.ofType<ScreenCreated>()
    val locationPermissionChanges = events
        .ofType<FacilitySelectionLocationPermissionChanged>()
        .map { it.result }

    val showProgressWhenPermissionIsAvailable = Observables.combineLatest(screenCreations, locationPermissionChanges)
        .take(1)
        .filter { (_, permissionResult) -> permissionResult == RuntimePermissionResult.GRANTED }
        .map { { ui: Ui -> ui.showProgressIndicator() } }

    val hideProgress = events
        .ofType<FacilitySelectionUserLocationUpdated>()
        .map { { ui: Ui -> ui.hideProgressIndicator() } }

    return showProgressWhenPermissionIsAvailable.mergeWith(hideProgress)
  }

  private fun showFacilities(events: Observable<UiEvent>): Observable<UiChange> {
    val searchQueryChanges = events
        .ofType<FacilitySelectionSearchQueryChanged>()
        .map { it.query }

    val locationUpdates = events
        .ofType<FacilitySelectionUserLocationUpdated>()
        .map { it.location }

    val filteredFacilityListItems = Observables
        .combineLatest(searchQueryChanges, locationUpdates, configProvider)
        .switchMap { (query, locationUpdate, config) ->
          val userLocation = when (locationUpdate) {
            is LocationUpdate.Available -> locationUpdate.location
            is LocationUpdate.Unavailable -> null
          }

          userSession.requireLoggedInUser()
              .switchMap { user -> facilityRepository.facilitiesInCurrentGroup(query, user) }
              .map {
                listItemBuilder.build(
                    facilities = it,
                    searchQuery = query,
                    userLocation = userLocation,
                    proximityThreshold = config.proximityThresholdForNearbyFacilities)
              }
        }

    return filteredFacilityListItems
        .map { listItems -> { ui: Ui -> ui.updateFacilities(listItems) } }
  }

  private fun toggleSearchFieldInToolbar(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<FacilitySelectionUserLocationUpdated>()
        .map { { ui: Ui -> ui.showToolbarWithSearchField() } }
        .startWith(Observable.just({ ui: Ui -> ui.showToolbarWithoutSearchField() }))
  }

  private fun changeFacilityAndExit(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<FacilitySelected>()
        .map { { ui: Ui -> ui.sendSelectedFacility(it.facility) } }
  }
}
