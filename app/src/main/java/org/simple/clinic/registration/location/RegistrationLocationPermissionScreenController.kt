package org.simple.clinic.registration.location

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationLocationPermissionScreen
typealias UiChange = (Ui) -> Unit

class RegistrationLocationPermissionScreenController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return handlePermissionGrants(replayedEvents)
  }

  private fun handlePermissionGrants(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationLocationPermissionChanged>()
        .filter { it.result == RuntimePermissionResult.GRANTED }
        .map { { ui: Ui -> ui.openFacilitySelectionScreen() } }
  }
}
