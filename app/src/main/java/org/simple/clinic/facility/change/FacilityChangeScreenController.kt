package org.simple.clinic.facility.change

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.ScreenCreated
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
    return events
        .ofType<ScreenCreated>()
        .flatMap { facilityRepository.facilities() }
        .map { { ui: Ui -> ui.updateFacilities(it) } }
  }

  private fun changeFacilityAndExit(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<FacilityClicked>()
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

