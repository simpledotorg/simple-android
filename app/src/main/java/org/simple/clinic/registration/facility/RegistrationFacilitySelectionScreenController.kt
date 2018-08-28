package org.simple.clinic.registration.facility

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.FacilitySync
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
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    val transformedEvents = replayedEvents
        .mergeWith(handleFacilitySelectionChanges(replayedEvents))

    return Observable.mergeArray(
        fetchFacilities(transformedEvents),
        showFacilities(transformedEvents),
        enableDoneButtonOnFacilitySelection(transformedEvents),
        proceedOnDoneClicks(transformedEvents))
  }

  private fun fetchFacilities(events: Observable<UiEvent>): Observable<UiChange> {
    val retryClicks = events.ofType<RegistrationFacilitySelectionRetryClicked>()

    val fetchOnScreenStart = events
        .ofType<ScreenCreated>()
        .flatMap { facilityRepository.facilities() }
        .take(1)
        .filter { it.isEmpty() }

    return Observable.merge(fetchOnScreenStart, retryClicks)
        .flatMap {
          facilitySync.pullWithResult()
              .toObservable()
              .flatMap {
                when (it) {
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

  private fun handleFacilitySelectionChanges(events: Observable<UiEvent>): Observable<UiEvent> {
    return events
        .ofType<RegistrationFacilitySelectionChanged>()
        .scan(emptySet<Facility>()) { selectedFacilities, changeEvent ->
          if (changeEvent.isSelected) {
            selectedFacilities + changeEvent.facility
          } else {
            selectedFacilities - changeEvent.facility
          }
        }
        .map(::RegistrationSelectedFacilitiesChanged)
  }

  private fun showFacilities(events: Observable<UiEvent>): Observable<UiChange> {
    val facilitiesStream = events
        .ofType<ScreenCreated>()
        .flatMap { facilityRepository.facilities() }

    val facilitySelections = events
        .ofType<RegistrationSelectedFacilitiesChanged>()
        .map { it.selectedFacilities }

    return Observables.combineLatest(facilitiesStream, facilitySelections)
        .map { (facilities, selectedFacilities) ->
          facilities
              .map { FacilityListItem(facility = it, isSelected = selectedFacilities.contains(it)) }
        }
        .map { { ui: Ui -> ui.updateFacilities(it) } }
  }

  private fun proceedOnDoneClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val selectedFacilityUuids = events
        .ofType<RegistrationSelectedFacilitiesChanged>()
        .map { it.selectedFacilities.map { it.uuid } }

    return events
        .ofType<RegistrationFacilitySelectionDoneClicked>()
        .withLatestFrom(selectedFacilityUuids)
        .flatMap { (_, selectedUuids) ->
          userSession.ongoingRegistrationEntry()
              .map { it.copy(facilityIds = selectedUuids) }
              .flatMapCompletable { userSession.saveOngoingRegistrationEntry(it) }
              .andThen(userSession.loginFromOngoingRegistrationEntry())
              .andThen(registrationScheduler.schedule())
              .andThen(Observable.just({ ui: Ui -> ui.openHomeScreen() }))
        }
  }

  private fun enableDoneButtonOnFacilitySelection(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationSelectedFacilitiesChanged>()
        .map { it.selectedFacilities.size }
        .map { selected ->
          when {
            selected > 0 -> { ui: Ui -> ui.enableDoneButton() }
            else -> { ui: Ui -> ui.disableDoneButton() }
          }
        }
  }
}
