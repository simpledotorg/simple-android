package org.simple.clinic.facility.change

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = FacilityChangeScreen
typealias UiChange = (Ui) -> Unit

class FacilityChangeScreenController @Inject constructor(
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.mergeArray(
        showFacilities(replayedEvents),
        changeFacilityAndExit(replayedEvents))
  }

  private fun showFacilities(events: Observable<UiEvent>): Observable<UiChange> {
    val filteredFacilities = events
        .ofType<FacilityChangeSearchQueryChanged>()
        .map { it.query }
        .switchMap { query -> facilityRepository.facilities(query) }
        .replay()
        .refCount()

    val firstUpdate = filteredFacilities
        .map { facilities -> facilities to FIRST_UPDATE }
        .take(1)

    val subsequentUpdates = filteredFacilities
        .map { facilities -> facilities to SUBSEQUENT_UPDATE }
        .skip(1)

    return firstUpdate
        .mergeWith(subsequentUpdates)
        .map { (facilities, updateType) ->
          { ui: Ui -> ui.updateFacilities(facilities, updateType) }
        }
  }

  private fun changeFacilityAndExit(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<FacilityChangeClicked>()
        .map { it.facility }
        .flatMap { facility ->
          userSession.requireLoggedInUser()
              .take(1)
              .flatMapCompletable {
                facilityRepository
                    .associateUserWithFacility(it, facility)
                    .andThen(facilityRepository.setCurrentFacility(it, facility))
              }
              .andThen(Observable.just({ ui: Ui -> ui.goBack() }))
        }
  }
}

