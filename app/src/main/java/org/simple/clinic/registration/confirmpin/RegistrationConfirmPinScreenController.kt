package org.simple.clinic.registration.confirmpin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.registration.RegistrationScheduler
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import javax.inject.Inject

typealias Ui = RegistrationConfirmPinScreen
typealias UiChange = (Ui) -> Unit

class RegistrationConfirmPinScreenController @Inject constructor(
    private val userSession: UserSession,
    private val registrationScheduler: RegistrationScheduler
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.mergeArray(
        preFillExistingDetails(replayedEvents),
        showValidationError(replayedEvents),
        hideValidationError(replayedEvents),
        resetPins(replayedEvents),
        updateOngoingEntryAndProceed(replayedEvents))
  }

  private fun preFillExistingDetails(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationConfirmPinScreenCreated>()
        .flatMapSingle {
          userSession.ongoingRegistrationEntry()
              .map { { ui: Ui -> ui.preFillUserDetails(it) } }
        }
  }

  private fun showValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    val pinTextChanges = events.ofType<RegistrationConfirmPinTextChanged>().map { it.confirmPin }

    return events
        .ofType<RegistrationConfirmPinDoneClicked>()
        .withLatestFrom(pinTextChanges)
        .flatMapSingle { (_, confirmPin) -> matchesWithPin(confirmPin) }
        .filter { pinMatches -> pinMatches.not() }
        .map { { ui: Ui -> ui.showPinMismatchError() } }
  }

  private fun hideValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationConfirmPinTextChanged>()
        .map { { ui: Ui -> ui.hidePinMismatchError() } }
  }

  private fun updateOngoingEntryAndProceed(events: Observable<UiEvent>): Observable<UiChange> {
    val pinTextChanges = events.ofType<RegistrationConfirmPinTextChanged>()
    val doneClicks = events.ofType<RegistrationConfirmPinDoneClicked>()

    return doneClicks
        .withLatestFrom(pinTextChanges.map { it.confirmPin })
        .flatMapSingle { (_, confirmPin) -> matchesWithPin(confirmPin).map { it to confirmPin } }
        .filter { (pinMatches, _) -> pinMatches }
        .flatMap { (_, confirmPin) ->
          userSession.ongoingRegistrationEntry()
              .map { it.copy(pinConfirmation = confirmPin, createdAt = Instant.now()) }
              .flatMapCompletable { userSession.saveOngoingRegistrationEntry(it) }
              .andThen(Observable.just({ ui: Ui -> ui.openFacilitySelectionScreen() }))
        }
  }

  private fun matchesWithPin(confirmPin: String) =
      userSession
          .ongoingRegistrationEntry()
          .map { ongoingEntry -> ongoingEntry.pin == confirmPin }

  private fun resetPins(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationResetPinClicked>()
        .flatMap {
          userSession.ongoingRegistrationEntry()
              .map { it.copy(pin = null, pinConfirmation = null) }
              .flatMapCompletable { userSession.saveOngoingRegistrationEntry(it) }
              .andThen(Observable.just({ ui: Ui -> ui.goBackToPinScreen() }))
        }
  }
}
