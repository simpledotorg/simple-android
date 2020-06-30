package org.simple.clinic.registration.facility

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.change.FacilityListItemBuilder
import org.simple.clinic.location.ScreenLocationUpdates
import org.simple.clinic.registration.RegistrationConfig
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
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

    return Observable.never()
  }
}
