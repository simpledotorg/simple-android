package org.simple.clinic.registration.location

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationLocationPermissionUi
typealias UiChange = (Ui) -> Unit

class RegistrationLocationPermissionScreenController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return handlePermissionGrants(replayedEvents)
  }

  private fun handlePermissionGrants(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RequestLocationPermission>()
        .filter(RequestLocationPermission::isPermissionGranted)
        .map { { ui: Ui -> ui.openFacilitySelectionScreen() } }
  }
}
