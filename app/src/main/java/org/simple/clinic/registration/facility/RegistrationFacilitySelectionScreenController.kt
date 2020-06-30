package org.simple.clinic.registration.facility

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.change.FacilityListItemBuilder
import org.simple.clinic.location.ScreenLocationUpdates
import org.simple.clinic.registration.RegistrationConfig
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.time.Instant
import javax.inject.Inject

typealias Ui = RegistrationFacilitySelectionUi
typealias UiChange = (Ui) -> Unit

class RegistrationFacilitySelectionScreenController @Inject constructor(
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession,
    private val config: RegistrationConfig,
    private val listItemBuilder: FacilityListItemBuilder,
    private val screenLocationUpdates: ScreenLocationUpdates,
    private val utcClock: UtcClock
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.mergeArray(
        toggleSearchFieldInToolbar(replayedEvents),
        proceedOnFacilityClicks(replayedEvents),
        proceedOnFacilityConfirmation(replayedEvents)
    )
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
        .map { facility -> { ui: Ui -> ui.showConfirmFacilitySheet(facility.uuid, facility.name) } }
  }

  private fun proceedOnFacilityConfirmation(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationFacilityConfirmed>()
        .map { it.facilityUuid }
        .map { facilityUuid ->
          val entry = userSession.ongoingRegistrationEntry().get()
          entry.copy(facilityId = facilityUuid)
        }
        .doOnNext(userSession::saveOngoingRegistrationEntry)
        .flatMap {
          userSession
              .saveOngoingRegistrationEntryAsUser(Instant.now(utcClock))
              .andThen(Observable.just { ui: Ui -> ui.openIntroVideoScreen() })
        }
  }
}
