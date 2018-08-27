package org.simple.clinic.registration.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationPinScreen
typealias UiChange = (Ui) -> Unit

class RegistrationPinScreenController @Inject constructor(
    val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.merge(
        preFillExistingDetails(replayedEvents),
        showValidationError(replayedEvents),
        hideValidationError(replayedEvents),
        updateOngoingEntryAndProceed(replayedEvents))
  }

  private fun preFillExistingDetails(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationPinScreenCreated>()
        .flatMapSingle {
          userSession.ongoingRegistrationEntry()
              .map { { ui: Ui -> ui.preFillUserDetails(it) } }
        }
  }

  private fun isPinValid(pin: String) = pin.length == 4

  private fun showValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    val pinTextChanges = events.ofType<RegistrationPinTextChanged>().map { it.pin }

    return events
        .ofType<RegistrationPinDoneClicked>()
        .withLatestFrom(pinTextChanges)
        .filter { (_, pin) -> isPinValid(pin).not() }
        .map { { ui: Ui -> ui.showIncompletePinError() } }
  }

  private fun hideValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationPinTextChanged>()
        .map { { ui: Ui -> ui.hideIncompletePinError() } }
  }

  private fun updateOngoingEntryAndProceed(events: Observable<UiEvent>): Observable<UiChange> {
    val pinTextChanges = events.ofType<RegistrationPinTextChanged>()
    val doneClicks = events.ofType<RegistrationPinDoneClicked>()

    return doneClicks
        .withLatestFrom(pinTextChanges.map { it.pin })
        .filter { (_, pin) -> isPinValid(pin) }
        .flatMap { (_, pin) ->
          if (pin.length > 4) {
            throw AssertionError("Shouldn't happen")
          }

          userSession.ongoingRegistrationEntry()
              .map { it.copy(pin = pin) }
              .flatMapCompletable { userSession.saveOngoingRegistrationEntry(it) }
              .andThen(Observable.just({ ui: Ui -> ui.openRegistrationConfirmPinScreen() }))
        }
  }
}
