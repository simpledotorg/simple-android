package org.simple.clinic.facility.change

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import java.util.concurrent.atomic.AtomicBoolean
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
    // Would have preferred RxJava for calculating this, but I couldn't make it work - Saket.
    val firstUpdateDone = AtomicBoolean(false)

    return events
        .ofType<FacilityChangeSearchQueryChanged>()
        .map { it.query }
        .switchMap { query -> facilityRepository.facilities(query) }
        .map { facilities ->
          val isFirstUpdate = firstUpdateDone.get().not()
          firstUpdateDone.set(true)
          return@map { ui: Ui -> ui.updateFacilities(facilities, isFirstUpdate) }
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

