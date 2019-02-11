package org.simple.clinic.facility.change

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
import org.simple.clinic.reports.ReportsRepository
import org.simple.clinic.reports.ReportsSync
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = FacilityChangeScreen
typealias UiChange = (Ui) -> Unit

class FacilityChangeScreenController @Inject constructor(
    private val facilityRepository: FacilityRepository,
    private val reportsRepository: ReportsRepository,
    private val userSession: UserSession,
    private val reportsSync: ReportsSync
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.mergeArray(
        showFacilities(replayedEvents),
        changeFacilityAndExit(replayedEvents))
  }

  private fun showFacilities(events: Observable<UiEvent>): Observable<UiChange> {
    val filteredFacilityListItems = events
        .ofType<FacilityChangeSearchQueryChanged>()
        .map { it.query }
        .switchMap { query ->
          userSession.requireLoggedInUser()
              .switchMap { user -> facilityRepository.facilitiesInCurrentGroup(query, user) }
              .map { facilities -> facilities.map { FacilityListItemBuilder.build(it, query) } }
        }
        .replay()
        .refCount()

    val firstUpdate = filteredFacilityListItems
        .map { listItems -> listItems to FIRST_UPDATE }
        .take(1)

    val subsequentUpdates = filteredFacilityListItems
        .map { listItems -> listItems to SUBSEQUENT_UPDATE }
        .skip(1)

    return firstUpdate
        .mergeWith(subsequentUpdates)
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
