package org.simple.clinic.scheduleappointment.facilityselection

import android.annotation.SuppressLint
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.change.FacilitiesUpdateType
import org.simple.clinic.facility.change.FacilityChangeConfig
import org.simple.clinic.facility.change.FacilityListItemBuilder
import org.simple.clinic.location.LocationRepository
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.scheduleappointment.patientFacilityTransfer.PatientFacilityChangeClicked
import org.simple.clinic.scheduleappointment.patientFacilityTransfer.PatientFacilityLocationPermissionChanged
import org.simple.clinic.scheduleappointment.patientFacilityTransfer.PatientFacilitySearchQueryChanged
import org.simple.clinic.scheduleappointment.patientFacilityTransfer.PatientFacilityUserLocationUpdated
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.ElapsedRealtimeClock
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.util.timer
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = FacilitySelectionActivity
typealias UiChange = (Ui) -> Unit

class FacilitySelectionActivityController @Inject constructor(
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession,
    private val locationRepository: LocationRepository,
    private val configProvider: Observable<FacilityChangeConfig>,
    private val elapsedRealtimeClock: ElapsedRealtimeClock,
    private val listItemBuilder: FacilityListItemBuilder
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
    val locationPermissionChanges = events
        .ofType<PatientFacilityLocationPermissionChanged>()
        .map { it.result }

    val locationUpdates = Observables
        .combineLatest(events.ofType<ScreenCreated>(), locationPermissionChanges)
        .switchMap { (_, permissionResult) ->
          when (permissionResult!!) {
            RuntimePermissionResult.GRANTED -> fetchLocation()
            RuntimePermissionResult.DENIED, RuntimePermissionResult.NEVER_ASK_AGAIN -> Observable.just(LocationUpdate.Unavailable)
          }
        }
        .take(1)
        .map { PatientFacilityUserLocationUpdated(it) }

    events.mergeWith(locationUpdates)
  }

  @SuppressLint("MissingPermission")
  private fun fetchLocation(): Observable<LocationUpdate> {
    val locationWaitExpiry = {
      configProvider
          .flatMap { Observables.timer(it.locationListenerExpiry) }
          .map { LocationUpdate.Unavailable }
    }

    val fetchLocation = {
      configProvider
          .flatMap { config ->
            locationRepository
                .streamUserLocation(config.locationUpdateInterval, Schedulers.io())
                .filter { isRecentLocation(it, config) }
          }
          .onErrorResumeNext(Observable.empty())
    }
    return Observable.merge(locationWaitExpiry(), fetchLocation())
  }

  private fun isRecentLocation(update: LocationUpdate, config: FacilityChangeConfig): Boolean {
    return when (update) {
      is LocationUpdate.Available -> update.age(elapsedRealtimeClock) <= config.staleLocationThreshold
      is LocationUpdate.Unavailable -> true
    }
  }

  private fun showProgressForReadingLocation(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreations = events.ofType<ScreenCreated>()
    val locationPermissionChanges = events
        .ofType<PatientFacilityLocationPermissionChanged>()
        .map { it.result }

    val showProgressWhenPermissionIsAvailable = Observables.combineLatest(screenCreations, locationPermissionChanges)
        .take(1)
        .filter { (_, permissionResult) -> permissionResult == RuntimePermissionResult.GRANTED }
        .map { { ui: Ui -> ui.showProgressIndicator() } }

    val hideProgress = events
        .ofType<PatientFacilityUserLocationUpdated>()
        .map { { ui: Ui -> ui.hideProgressIndicator() } }

    return showProgressWhenPermissionIsAvailable.mergeWith(hideProgress)
  }

  private fun showFacilities(events: Observable<UiEvent>): Observable<UiChange> {
    val searchQueryChanges = events
        .ofType<PatientFacilitySearchQueryChanged>()
        .map { it.query }

    val locationUpdates = events
        .ofType<PatientFacilityUserLocationUpdated>()
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
        .replay()
        .refCount()

    val firstUpdate = filteredFacilityListItems
        .map { listItems -> listItems to FacilitiesUpdateType.FIRST_UPDATE }
        .take(1)

    val subsequentUpdates = filteredFacilityListItems
        .map { listItems -> listItems to FacilitiesUpdateType.SUBSEQUENT_UPDATE }
        .skip(1)

    return Observable.merge(firstUpdate, subsequentUpdates)
        .map { (listItems, updateType) ->
          { ui: Ui -> ui.updateFacilities(listItems, updateType) }
        }
  }

  private fun toggleSearchFieldInToolbar(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientFacilityUserLocationUpdated>()
        .map { { ui: Ui -> ui.showToolbarWithSearchField() } }
        .startWith(Observable.just({ ui: Ui -> ui.showToolbarWithoutSearchField() }))
  }

  private fun changeFacilityAndExit(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientFacilityChangeClicked>()
        .map { it.facility.uuid }
        .map { { ui: Ui -> ui.sendSelectedFacility(it) } }
  }
}
