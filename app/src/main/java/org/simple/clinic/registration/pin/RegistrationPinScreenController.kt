package org.simple.clinic.registration.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationPinScreen
typealias UiChange = (Ui) -> Unit

class RegistrationPinScreenController @Inject constructor(
    val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.merge(
        preFillExistingDetails(replayedEvents),
        enableNextButton(replayedEvents),
        disableNextButton(replayedEvents),
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

  private fun updateOngoingEntryAndProceed(events: Observable<UiEvent>): Observable<UiChange> {
    val pinTextChanges = events.ofType<RegistrationPinTextChanged>()
    val nextClicks = events.ofType<RegistrationPinNextClicked>()

    return nextClicks
        .withLatestFrom(pinTextChanges.map { it.pin })
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

  private fun enableNextButton(events: Observable<UiEvent>): Observable<UiChange> {
    return setNextButtonEnabled(events, true)
  }

  private fun disableNextButton(events: Observable<UiEvent>): Observable<UiChange> {
    return setNextButtonEnabled(events, false)
  }

  private fun setNextButtonEnabled(events: Observable<UiEvent>, enabled: Boolean): Observable<UiChange> {
    return events
        .ofType<RegistrationPinTextChanged>()
        .map { it.pin.isBlank() }
        .distinctUntilChanged()
        .filter { isBlank -> isBlank != enabled }
        .map { { ui: Ui -> ui.setNextButtonEnabled(enabled) } }
  }
}
