package org.simple.clinic.forgotpin.createnewpin

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

typealias Ui = ForgotPinCreateNewPinScreen
typealias UiChange = (Ui) -> Unit

class ForgotPinCreateNewPinScreenController @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(upstream: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = upstream.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.merge(
        showUserNameOnScreenCreate(replayedEvents),
        showFacilityOnScreenCreate(replayedEvents)
    )
  }

  private fun showUserNameOnScreenCreate(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .firstOrError()
        .flatMap { userSession.requireLoggedInUser().firstOrError() }
        .map { { ui: Ui -> ui.showUserName(it.fullName) } }
        .toObservable()
  }

  private fun showFacilityOnScreenCreate(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .firstOrError()
        .flatMap { userSession.requireLoggedInUser().firstOrError() }
        .flatMap { facilityRepository.currentFacility(it).firstOrError() }
        .map { { ui: Ui -> ui.showFacility(it.name) } }
        .toObservable()
  }

}
