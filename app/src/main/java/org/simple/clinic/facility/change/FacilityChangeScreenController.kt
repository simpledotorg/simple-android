package org.simple.clinic.facility.change

import android.annotation.SuppressLint
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
import org.simple.clinic.location.LocationRepository
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.location.LocationUpdate.Available
import org.simple.clinic.location.LocationUpdate.Unavailable
import org.simple.clinic.reports.ReportsRepository
import org.simple.clinic.reports.ReportsSync
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.ElapsedRealtimeClock
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.RuntimePermissionResult.NEVER_ASK_AGAIN
import org.simple.clinic.util.timer
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = FacilityChangeScreen
typealias UiChange = (Ui) -> Unit

class FacilityChangeScreenController @Inject constructor(
    private val facilityRepository: FacilityRepository,
    private val reportsRepository: ReportsRepository,
    private val userSession: UserSession,
    private val reportsSync: ReportsSync,
    private val locationRepository: LocationRepository,
    private val configProvider: Single<FacilityChangeConfig>,
    private val elapsedRealtimeClock: ElapsedRealtimeClock
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(fetchLocation())
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        showProgressForReadingLocation(replayedEvents),
        showFacilities(replayedEvents),
        changeFacilityAndExit(replayedEvents))
  }

  @SuppressLint("MissingPermission")
  private fun fetchLocation() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val locationPermissionChanges = events
        .ofType<FacilityChangeLocationPermissionChanged>()
        .map { it.result }

    val locationWaitExpiry = {
      configProvider
          .flatMapObservable { Observables.timer(it.locationListenerExpiry) }
          .map { Unavailable }
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
        .map { FacilityChangeUserLocationUpdated(it) }

    events.mergeWith(locationUpdates)
  }

  private fun isRecentLocation(update: LocationUpdate, config: FacilityChangeConfig): Boolean {
    return when (update) {
      is Available -> update.age(elapsedRealtimeClock) <= config.staleLocationThreshold
      is Unavailable -> true
    }
  }

  private fun showProgressForReadingLocation(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreations = events.ofType<ScreenCreated>()
    val locationPermissionChanges = events
        .ofType<FacilityChangeLocationPermissionChanged>()
        .map { it.result }

    val showProgressWhenPermissionIsAvailable = Observables.combineLatest(screenCreations, locationPermissionChanges)
        .take(1)
        .filter { (_, permissionResult) -> permissionResult == GRANTED }
        .map { { ui: Ui -> ui.showProgressIndicator() } }

    val hideProgress = events
        .ofType<FacilityChangeUserLocationUpdated>()
        .map { { ui: Ui -> ui.hideProgressIndicator() } }

    return showProgressWhenPermissionIsAvailable.mergeWith(hideProgress)
  }

  private fun showFacilities(events: Observable<UiEvent>): Observable<UiChange> {
    val searchQueryChanges = events
        .ofType<FacilityChangeSearchQueryChanged>()
        .map { it.query }

    val locationUpdates = events
        .ofType<FacilityChangeUserLocationUpdated>()
        .map { it.location }

    val filteredFacilityListItems = Observables
        .combineLatest(searchQueryChanges, locationUpdates, configProvider.toObservable())
        .switchMap { (query, locationUpdate, config) ->
          val userLocation = when (locationUpdate) {
            is Available -> locationUpdate.location
            is Unavailable -> null
          }

          userSession.requireLoggedInUser()
              .switchMap { user -> facilityRepository.facilitiesInCurrentGroup(query, user) }
              .map {
                FacilityListItemBuilder.build(
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

  private fun changeFacilityAndExit(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<FacilityChangeClicked>()
        .map { it.facility }
        .flatMapSingle { facility ->
          userSession.requireLoggedInUser()
              .take(1)
              .flatMapCompletable {
                facilityRepository
                    .associateUserWithFacility(it, facility)
                    .andThen(facilityRepository.setCurrentFacility(it, facility))
              }
              .doOnComplete { clearAndSyncReports() }
              .andThen(Single.just(Ui::goBack))
        }
  }

  private fun clearAndSyncReports() {
    reportsRepository
        .deleteReportsFile()
        .toCompletable()
        .andThen(reportsSync.sync().onErrorComplete())
        .subscribeOn(Schedulers.io())
        .subscribe()
  }
}
