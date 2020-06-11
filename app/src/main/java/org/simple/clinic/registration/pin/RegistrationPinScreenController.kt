package org.simple.clinic.registration.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationPinUi
typealias UiChange = (Ui) -> Unit

class RegistrationPinScreenController @Inject constructor(
    val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .compose(autoSubmitPin())
        .replay()

    return Observable.merge(
        showValidationError(replayedEvents),
        hideValidationError(replayedEvents),
        updateOngoingEntryAndProceed(replayedEvents))
  }

  private fun autoSubmitPin(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { events ->
      val doneClicksStream = events
          .ofType<RegistrationPinTextChanged>()
          .filter { isPinValid(it.pin) }
          .map { RegistrationPinDoneClicked() }

      events.mergeWith(doneClicksStream)
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
        .ofType<RegistrationPinDoneClicked>()
        .map { { ui: Ui -> ui.hideIncompletePinError() } }
  }

  private fun updateOngoingEntryAndProceed(events: Observable<UiEvent>): Observable<UiChange> {
    val doneClicks = events.ofType<RegistrationPinDoneClicked>()
    val pinTextChanges = events
        .ofType<RegistrationPinTextChanged>()
        .map { it.pin }

    return Observables
        .combineLatest(doneClicks, pinTextChanges) { _, pin -> pin}
        .filter(::isPinValid)
        .doOnNext { pin ->
          // TODO (vs) 09/06/20: Remove this magic number from here and move it where it makes sense
          if (pin.length > 4) {
            throw AssertionError("Shouldn't happen")
          }
        }
        .map { pin ->
          val entry = (userSession.ongoingRegistrationEntry() as Just).value

          entry.copy(pin = pin)
        }
        .doOnNext(userSession::saveOngoingRegistrationEntry)
        .map { { ui: Ui -> ui.openRegistrationConfirmPinScreen() } }
  }
}
