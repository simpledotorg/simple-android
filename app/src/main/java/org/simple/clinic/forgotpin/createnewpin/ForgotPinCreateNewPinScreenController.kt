package org.simple.clinic.forgotpin.createnewpin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = ForgotPinCreateNewPinUi
typealias UiChange = (Ui) -> Unit

class ForgotPinCreateNewPinScreenController @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository
) : ObservableTransformer<UiEvent, UiChange> {

  private val pinLength = 4

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.mergeArray(
        showInvalidPinErrorOnIncompletePin(replayedEvents),
        openConfirmPinEntryScreenOnValidPin(replayedEvents),
        hidePinValidationErrors(replayedEvents))
  }

  private fun showInvalidPinErrorOnIncompletePin(events: Observable<UiEvent>): Observable<UiChange> {
    val pinTextChanges = events.ofType<ForgotPinCreateNewPinTextChanged>()
        .map { it.pin }

    return events
        .ofType<ForgotPinCreateNewPinSubmitClicked>()
        .withLatestFrom(pinTextChanges) { _, pin -> pin }
        .filter { it.length != pinLength }
        .map { { ui: Ui -> ui.showInvalidPinError() } }
  }

  private fun openConfirmPinEntryScreenOnValidPin(events: Observable<UiEvent>): Observable<UiChange> {
    val validPin = events.ofType<ForgotPinCreateNewPinTextChanged>()
        .map { it.pin }
        .filter { it.length == pinLength }

    return events
        .ofType<ForgotPinCreateNewPinSubmitClicked>()
        .withLatestFrom(validPin) { _, pin -> pin }
        .map { { ui: Ui -> ui.showConfirmPinScreen(it) } }
  }

  private fun hidePinValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ForgotPinCreateNewPinTextChanged>()
        .map { { ui: Ui -> ui.hideInvalidPinError() } }
  }
}
