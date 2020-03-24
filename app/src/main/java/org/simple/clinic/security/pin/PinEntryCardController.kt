package org.simple.clinic.security.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.security.ComparisonResult.DIFFERENT
import org.simple.clinic.security.ComparisonResult.SAME
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState
import org.simple.clinic.security.pin.PinEntryUi.State
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PinEntryUi
typealias UiChange = (Ui) -> Unit

class PinEntryCardController @Inject constructor(
    private val passwordHasher: PasswordHasher,
    private val utcClock: UtcClock,
    private val bruteForceProtection: BruteForceProtection
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(autoSubmitPin())
        .replay()

    return Observable.merge(
        validatePin(replayedEvents),
        blockWhenAuthenticationLimitIsReached(replayedEvents)
    )
  }

  private fun autoSubmitPin(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { events ->
      val pinSubmitClicks = events
          .ofType<PinTextChanged>()
          .filter { it.pin.length == 4 }
          .map { PinSubmitClicked(it.pin) }

      events.mergeWith(pinSubmitClicks)
    }
  }

  private fun validatePin(events: Observable<UiEvent>): Observable<UiChange> {
    val pinDigestToVerifyStream = events
        .ofType<PinDigestToVerify>()
        .map { it.pinDigest }

    val submittedPinStream = events
        .ofType<PinSubmitClicked>()
        .map { it.pin }

    return Observables.combineLatest(pinDigestToVerifyStream, submittedPinStream)
        .flatMap { (pinDigestToVerify, submittedPin) ->
          val cachedPinValidation = passwordHasher.compare(pinDigestToVerify, submittedPin)
              .toObservable()
              .replay()
              .refCount()

          val progressUiChanges = cachedPinValidation
              .filter { it == DIFFERENT }
              .map { { ui: Ui -> ui.moveToState(State.PinEntry) } }
              .startWith { ui: Ui ->
                ui.hideError()
                ui.moveToState(State.Progress)
              }

          val recordAttempts = cachedPinValidation
              .switchMap {
                when (it) {
                  SAME -> bruteForceProtection.recordSuccessfulAuthentication()
                  DIFFERENT -> bruteForceProtection.incrementFailedAttempt()
                }.andThen(Observable.empty<UiChange>())
              }

          val validationResultUiChange = cachedPinValidation
              .map {
                when (it) {
                  SAME -> { ui: Ui -> ui.dispatchAuthenticatedCallback(submittedPin) }
                  DIFFERENT -> { ui: Ui -> ui.clearPin() }
                }
              }

          Observable.mergeArray(progressUiChanges, recordAttempts, validationResultUiChange)
        }
  }

  private fun blockWhenAuthenticationLimitIsReached(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinEntryViewCreated>()
        .flatMap { bruteForceProtection.protectedStateChanges() }
        .switchMap { state ->
          when (state) {
            is ProtectedState.Allowed -> {
              Observable.just({ ui: Ui -> ui.moveToState(State.PinEntry) })
            }

            is ProtectedState.Blocked -> {
              Observable.just({ ui: Ui -> ui.moveToState(State.BruteForceLocked(state.blockedTill)) })
            }
          }
        }
  }
}
