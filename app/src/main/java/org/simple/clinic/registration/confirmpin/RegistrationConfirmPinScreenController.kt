package org.simple.clinic.registration.confirmpin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import javax.inject.Inject

typealias Ui = RegistrationConfirmPinScreen
typealias UiChange = (Ui) -> Unit

class RegistrationConfirmPinScreenController @Inject constructor(
    private val userSession: UserSession,
    private val clock: Clock
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    val transformedEvents = replayedEvents
        .compose(autoSubmitPin())
        .compose(validatePin())

    return Observable.merge(
        showValidationError(transformedEvents),
        resetPins(transformedEvents),
        saveConfirmPinAndProceed(transformedEvents))
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
        .flatMap {
          userSession.ongoingRegistrationEntry()
              .map { entry -> entry.copy(pinConfirmation = it.confirmPin, createdAt = Instant.now(clock)) }
              .flatMapCompletable { entry -> userSession.saveOngoingRegistrationEntry(entry) }
              .andThen(Observable.just({ ui: Ui -> ui.openFacilitySelectionScreen() }))
        }
  }

  private fun resetPins(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationResetPinClicked>()
        .flatMap {
          userSession.ongoingRegistrationEntry()
              .map { entry -> entry.copy(pin = null, pinConfirmation = null) }
              .flatMapCompletable { entry -> userSession.saveOngoingRegistrationEntry(entry) }
              .andThen(Observable.just({ ui: Ui -> ui.goBackToPinScreen() }))
        }
  }
}
