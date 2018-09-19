package org.simple.clinic.forgotpin.confirmpin

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

typealias Ui = ForgotPinConfirmPinScreen
typealias UiChange = (Ui) -> Unit

class ForgotPinConfirmPinScreenController @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(upstream: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = upstream.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.merge(
        showUserNameOnScreenStarted(replayedEvents),
        showFacilityOnScreenCreated(replayedEvents),
        openFacilityChangeScreen(replayedEvents),
        goBack(replayedEvents)
    )
  }

  private fun showUserNameOnScreenStarted(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ScreenCreated>()
        .flatMap { userSession.requireLoggedInUser() }
        .map { user -> { ui: Ui -> ui.showUserName(user.fullName) } }
  }

  private fun showFacilityOnScreenCreated(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ScreenCreated>()
        .flatMap { userSession.requireLoggedInUser() }
        .switchMap { facilityRepository.currentFacility(it) }
        .map { facility -> { ui: Ui -> ui.showFacility(facility.name) } }
  }

  private fun openFacilityChangeScreen(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ForgotPinConfirmPinScreenFacilityClicked>()
        .map { { ui: Ui -> ui.openFacilityChangeScreen() } }
  }

  private fun goBack(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ForgotPinConfirmPinScreenBackClicked>()
        .map { { ui: Ui -> ui.goBack() } }
  }
}
