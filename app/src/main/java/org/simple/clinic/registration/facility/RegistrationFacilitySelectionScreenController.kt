package org.simple.clinic.registration.facility

import android.annotation.SuppressLint
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilityPullResult.NetworkError
import org.simple.clinic.facility.FacilityPullResult.Success
import org.simple.clinic.facility.FacilityPullResult.UnexpectedError
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
import org.simple.clinic.facility.change.FacilityListItemBuilder
import org.simple.clinic.location.LocationRepository
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.location.LocationUpdate.Available
import org.simple.clinic.location.LocationUpdate.Unavailable
import org.simple.clinic.registration.RegistrationConfig
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.ElapsedRealtimeClock
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.RuntimePermissionResult.NEVER_ASK_AGAIN
import org.simple.clinic.util.timer
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationFacilitySelectionScreen
typealias UiChange = (Ui) -> Unit

class RegistrationFacilitySelectionScreenController @Inject constructor(
    private val facilitySync: FacilitySync,
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession,
    private val locationRepository: LocationRepository,
    private val configProvider: Single<RegistrationConfig>,
    private val elapsedRealtimeClock: ElapsedRealtimeClock,
    private val listItemBuilder: FacilityListItemBuilder
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(fetchLocation())
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        fetchFacilities(replayedEvents),
        showFacilities(replayedEvents),
        toggleSearchFieldInToolbar(replayedEvents),
        proceedOnFacilityClicks(replayedEvents))
  }

  @SuppressLint("MissingPermission")
  private fun fetchLocation() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val locationPermissionChanges = events
        .ofType<RegistrationFacilityLocationPermissionChanged>()
        .map { it.result }

    val locationWaitExpiry = {
      configProvider
          .flatMapObservable { Observables.timer(it.locationListenerExpiry) }
          .map { LocationUpdate.Unavailable }
    }

    val fetchLocation = {
      configProvider
          .flatMapObservable { config ->
            locationRepository
                .streamUserLocation(config.locationUpdateInterval, io())
                .filter { isRecentLocation(it, config) }
          }
          .onErrorResumeNext(Observable.empty())
    }

    val locationUpdates = Observables
        .combineLatest(events.ofType<ScreenCreated>(), locationPermissionChanges)
        .switchMap { (_, permissionResult) ->
          when (permissionResult!!) {
            GRANTED -> Observable.merge(locationWaitExpiry(), fetchLocation())
            DENIED, NEVER_ASK_AGAIN -> Observable.just(Unavailable)
          }
        }
        .take(1)
        .map { RegistrationFacilityUserLocationUpdated(it) }

    events.mergeWith(locationUpdates)
  }

  private fun isRecentLocation(update: LocationUpdate, config: RegistrationConfig): Boolean {
    return when (update) {
      is Available -> update.age(elapsedRealtimeClock) <= config.staleLocationThreshold
      is Unavailable -> true
    }
  }

  private fun fetchFacilities(events: Observable<UiEvent>): Observable<UiChange> {
    val fetchFacilitiesOnStart = events
        .ofType<ScreenCreated>()
        .flatMap { facilityRepository.recordCount() }
        .take(1)
        .flatMapSingle { count ->
          when (count) {
            0 -> facilitySync.pullWithResult()
            else -> Single.just(FacilityPullResult.Success())
          }
        }

    val fetchFacilitiesOnRetry = events
        .ofType<RegistrationFacilitySelectionRetryClicked>()
        .switchMap { facilitySync.pullWithResult().toObservable() }

    val fetchFacilities = fetchFacilitiesOnStart.mergeWith(fetchFacilitiesOnRetry)

    val locationUpdates = events.ofType<RegistrationFacilityUserLocationUpdated>()

    // We don't care about the location here. We just want to show
    // progress until we receive an update, even if it's empty.
    return Observables.combineLatest(fetchFacilities, locationUpdates)
        .flatMap { (facilityPullResult, _) ->
          when (facilityPullResult) {
            is Success -> Observable.just(
                { ui: Ui -> ui.hideProgressIndicator() })
            is NetworkError -> Observable.just(
                { ui: Ui -> ui.hideProgressIndicator() },
                { ui: Ui -> ui.showNetworkError() })
            is UnexpectedError -> Observable.just(
                { ui: Ui -> ui.hideProgressIndicator() },
                { ui: Ui -> ui.showUnexpectedError() })
          }
        }
        .startWith(Observable.just(
            { ui: Ui -> ui.hideError() },
            { ui: Ui -> ui.showProgressIndicator() }))
  }

  private fun showFacilities(events: Observable<UiEvent>): Observable<UiChange> {
    val searchQueryChanges = events
        .ofType<RegistrationFacilitySearchQueryChanged>()
        .map { it.query }

    val locationUpdates = events
        .ofType<RegistrationFacilityUserLocationUpdated>()
        .map { it.location }

    val filteredFacilityListItems = Observables
        .combineLatest(searchQueryChanges, locationUpdates, configProvider.toObservable())
        .switchMap { (query, locationUpdate, config) ->
          val userLocation = when (locationUpdate) {
            is Available -> locationUpdate.location
            is Unavailable -> null
          }

          facilityRepository
              .facilities(query)
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
        .map { listItems -> listItems to FIRST_UPDATE }
        .take(1)

    val subsequentUpdates = filteredFacilityListItems
        .map { listItems -> listItems to SUBSEQUENT_UPDATE }
        .skip(1)

    return Observable.merge(firstUpdate, subsequentUpdates)
        .map { (listItems, updateType) ->
          { ui: Ui -> ui.updateFacilities(listItems, updateType) }
        }
  }

  private fun toggleSearchFieldInToolbar(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .flatMap { facilityRepository.recordCount() }
        .map { count -> count > 0 }
        .distinctUntilChanged()
        .map { hasFacilities ->
          if (hasFacilities) {
            { ui: Ui -> ui.showToolbarWithSearchField() }
          } else {
            { ui: Ui -> ui.showToolbarWithoutSearchField() }
          }
        }
  }

  private fun proceedOnFacilityClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationFacilityClicked>()
        .map { it.facility }
        .flatMap { facility ->
          userSession.ongoingRegistrationEntry()
              .map { it.copy(facilityId = facility.uuid) }
              .flatMapCompletable { userSession.saveOngoingRegistrationEntry(it) }
              .andThen(userSession.loginFromOngoingRegistrationEntry())
              .andThen(Observable.just({ ui: Ui -> ui.openRegistrationScreen() }))
        }
  }
}
