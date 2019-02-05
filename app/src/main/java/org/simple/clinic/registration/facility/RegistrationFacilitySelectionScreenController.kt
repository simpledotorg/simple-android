package org.simple.clinic.registration.facility

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
import org.simple.clinic.facility.change.FacilityListItem
import org.simple.clinic.registration.RegistrationScheduler
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationFacilitySelectionScreen
typealias UiChange = (Ui) -> Unit

class RegistrationFacilitySelectionScreenController @Inject constructor(
    private val facilitySync: FacilitySync,
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession,
    private val registrationScheduler: RegistrationScheduler
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        fetchFacilities(replayedEvents),
        showFacilities(replayedEvents),
        toggleSearchFieldInToolbar(replayedEvents),
        proceedOnFacilityClicks(replayedEvents))
  }

  private fun fetchFacilities(events: Observable<UiEvent>): Observable<UiChange> {
    val retryClicks = events.ofType<RegistrationFacilitySelectionRetryClicked>()

    val fetchOnScreenStart = events
        .ofType<ScreenCreated>()
        .flatMap { facilityRepository.recordCount() }
        .take(1)
        .filter { count -> count == 0 }

    return Observable.merge(fetchOnScreenStart, retryClicks)
        .flatMap {
          facilitySync.pullWithResult()
              .toObservable()
              .flatMap { result ->
                when (result) {
                  is FacilityPullResult.Success -> Observable.just({ ui: Ui -> ui.hideProgressIndicator() })
                  is FacilityPullResult.NetworkError -> Observable.just(
                      { ui: Ui -> ui.hideProgressIndicator() },
                      { ui: Ui -> ui.showNetworkError() })
                  is FacilityPullResult.UnexpectedError -> Observable.just(
                      { ui: Ui -> ui.hideProgressIndicator() },
                      { ui: Ui -> ui.showUnexpectedError() })
                }
              }
              .startWith(Observable.just({ ui: Ui -> ui.hideError() }, { ui: Ui -> ui.showProgressIndicator() }))
        }
  }

  private fun showFacilities(events: Observable<UiEvent>): Observable<UiChange> {
    val filteredFacilityListItems = events
        .ofType<RegistrationFacilitySearchQueryChanged>()
        .map { it.query }
        .switchMap { query ->
          facilityRepository
              .facilities(query)
              .map { facilities -> facilities.map { FacilityListItem.Builder.build(it, query) } }
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
              .map { it.copy(facilityIds = listOf(facility.uuid)) }
              .flatMapCompletable { userSession.saveOngoingRegistrationEntry(it) }
              .andThen(userSession.loginFromOngoingRegistrationEntry())
              .andThen(registrationScheduler.schedule())
              .andThen(Observable.just({ ui: Ui -> ui.openHomeScreen() }))
        }
  }
}
