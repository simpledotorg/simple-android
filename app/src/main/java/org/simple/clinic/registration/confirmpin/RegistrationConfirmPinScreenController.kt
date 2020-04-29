package org.simple.clinic.registration.confirmpin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationConfirmPinScreen
typealias UiChange = (Ui) -> Unit

class RegistrationConfirmPinScreenController @Inject constructor(
    private val userSession: UserSession,
    private val utcClock: UtcClock
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(autoSubmitPin())
        .compose(validatePin())
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.merge(
        showValidationError(replayedEvents),
        resetPins(replayedEvents),
        saveConfirmPinAndProceed(replayedEvents))
  }

  private fun autoSubmitPin(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { upstream ->
      val autoSubmits = upstream
          .ofType<RegistrationConfirmPinTextChanged>()
          .distinctUntilChanged()
          .filter { it.confirmPin.length == 4 }
          .map { RegistrationConfirmPinDoneClicked() }
      upstream.mergeWith(autoSubmits)
    }
  }

  private fun validatePin(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { upstream ->
      val doneClicks = upstream.ofType<RegistrationConfirmPinDoneClicked>()

      val pinTextChanges = upstream
          .ofType<RegistrationConfirmPinTextChanged>()
          .map { it.confirmPin }

      val validations = doneClicks
          .withLatestFrom(pinTextChanges)
          .flatMapSingle { (_, confirmPin) ->
            userSession
                .ongoingRegistrationEntry()
                .map { ongoingEntry ->
                  val valid = ongoingEntry.pin == confirmPin
                  RegistrationConfirmPinValidated(confirmPin, valid)
                }
          }

      upstream.mergeWith(validations)
    }
  }

  private fun showValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationConfirmPinValidated>()
        .filter { it.valid.not() }
        .map {
          { ui: Ui ->
            ui.showPinMismatchError()
            ui.clearPin()
          }
        }
  }

  private fun saveConfirmPinAndProceed(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationConfirmPinValidated>()
        .filter { it.valid }
        .flatMap { confirmPinValidated ->
          userSession.ongoingRegistrationEntry()
              .map { entry -> entry.withPinConfirmation(pinConfirmation = confirmPinValidated.enteredPin, clock = utcClock) }
              .flatMapCompletable { entry -> userSession.saveOngoingRegistrationEntry(entry) }
              .andThen(Observable.just({ ui: Ui -> ui.openFacilitySelectionScreen() }))
        }
  }

  private fun resetPins(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationResetPinClicked>()
        .flatMap {
          userSession.ongoingRegistrationEntry()
              .map(OngoingRegistrationEntry::resetPin)
              .flatMapCompletable { entry -> userSession.saveOngoingRegistrationEntry(entry) }
              .andThen(Observable.just({ ui: Ui -> ui.goBackToPinScreen() }))
        }
  }
}
