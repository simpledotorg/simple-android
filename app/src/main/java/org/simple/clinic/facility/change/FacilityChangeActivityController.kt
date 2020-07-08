package org.simple.clinic.facility.change

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = FacilityChangeActivity
typealias UiChange = (Ui) -> Unit

class FacilityChangeActivityController @Inject constructor(
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return confirmFacilityChange(replayedEvents)
  }

  private fun confirmFacilityChange(events: Observable<UiEvent>): Observable<UiChange> {
    val currentFacility = events
        .ofType<ScreenCreated>()
        .flatMap { userSession.loggedInUser() }
        .filterAndUnwrapJust()
        .switchMap { facilityRepository.currentFacility(it) }
        .share()

    val facilityStreams = events
        .ofType<FacilityChangeClicked>()
        .map { it.facility }
        .withLatestFrom(currentFacility)

    val newFacilitySelected = facilityStreams
        .filter { (selectedFacility, currentFacility) -> selectedFacility.uuid != currentFacility.uuid }
        .map { (selectedFacility, _) -> selectedFacility }
        .map { { ui: Ui -> ui.openConfirmationSheet(it) } }

    val sameFacilitySelected = facilityStreams
        .filter { (selectedFacility, currentFacility) -> selectedFacility.uuid == currentFacility.uuid }
        .map { Ui::goBack }

    return newFacilitySelected.mergeWith(sameFacilitySelected)
  }
}
