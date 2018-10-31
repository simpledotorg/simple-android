package org.simple.clinic.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.login.applock.ComparisonResult
import org.simple.clinic.login.applock.ComparisonResult.DIFFERENT
import org.simple.clinic.login.applock.ComparisonResult.SAME
import org.simple.clinic.login.applock.PasswordHasher
import org.simple.clinic.pin.PinEntryCardView.State
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PinEntryCardView
typealias UiChange = (Ui) -> Unit

class PinEntryCardController @Inject constructor(
    private val userSession: UserSession,
    private val passwordHasher: PasswordHasher
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    val transformedEvents = events
        .mergeWith(autoSubmitPin(replayedEvents))

    return Observable.merge(
        validatePin(transformedEvents),
        removeErrorOnSubmit(transformedEvents))
  }

  private fun autoSubmitPin(events: Observable<UiEvent>): Observable<UiEvent> {
    return events
        .ofType<PinTextChanged>()
        .filter { it.pin.length == 4 }
        .map { PinSubmitClicked(it.pin) }
  }

  private fun validatePin(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinSubmitClicked>()
        .map { it.pin }
        .flatMap { pin ->
          val cachedPinValidation = userSession.requireLoggedInUser()
              .map { it.pinDigest }
              .firstOrError()
              .flatMap { pinDigest -> passwordHasher.compare(pinDigest, pin) }
              .cache()

          val validationResultUiChange = cachedPinValidation
              .map {
                { ui: Ui ->
                  when (it) {
                    SAME -> ui.dispatchAuthenticatedCallback()
                    DIFFERENT -> {
                      ui.showIncorrectPinError()
                      ui.clearPin()
                    }
                  }
                }
              }
              .toObservable()

          val progressUiChanges = cachedPinValidation
              .filter { it == ComparisonResult.DIFFERENT }
              .map { { ui: Ui -> ui.moveToState(State.PinEntry) } }
              .toObservable()
              .startWith { ui: Ui -> ui.moveToState(State.Progress) }

          Observable.mergeArray(progressUiChanges, validationResultUiChange)
        }
  }

  private fun removeErrorOnSubmit(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PinSubmitClicked>()
        .map { { ui: Ui -> ui.hideError() } }
  }
}
