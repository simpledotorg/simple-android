package org.simple.clinic.registration.confirmpin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import javax.inject.Inject

typealias Ui = RegistrationConfirmPinUi
typealias UiChange = (Ui) -> Unit

class RegistrationConfirmPinScreenController @Inject constructor(
    private val userSession: UserSession,
    private val utcClock: UtcClock
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(validatePin())
        .replay()

    return Observable.merge(
        resetPins(replayedEvents),
        saveConfirmPinAndProceed(replayedEvents))
  }

  private fun validatePin(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { upstream ->
      val doneClicks = upstream.ofType<RegistrationConfirmPinDoneClicked>()

      val pinTextChanges = upstream
          .ofType<RegistrationConfirmPinTextChanged>()
          .map { it.confirmPin }

      val validations = doneClicks
          .withLatestFrom(pinTextChanges) { _, confirmPin -> ongoingRegistrationEntry() to confirmPin }
          .map { (currentEntry, confirmPin) ->
            val valid = currentEntry.pin == confirmPin
            RegistrationConfirmPinValidated(confirmPin, valid)
          }

      upstream.mergeWith(validations)
    }
  }

  private fun saveConfirmPinAndProceed(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationConfirmPinValidated>()
        .filter { it.valid }
        .map { confirmPinValidated ->
          ongoingRegistrationEntry().withPinConfirmation(
              pinConfirmation = confirmPinValidated.enteredPin,
              timestamp = Instant.now(utcClock)
          )
        }
        .doOnNext(userSession::saveOngoingRegistrationEntry)
        .map { { ui: Ui -> ui.openFacilitySelectionScreen() } }
  }

  private fun resetPins(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationResetPinClicked>()
        .map { ongoingRegistrationEntry() }
        .map(OngoingRegistrationEntry::resetPin)
        .doOnNext(userSession::saveOngoingRegistrationEntry)
        .map { { ui: Ui -> ui.goBackToPinScreen() } }
  }

  private fun ongoingRegistrationEntry(): OngoingRegistrationEntry = (userSession.ongoingRegistrationEntry() as Just).value
}
