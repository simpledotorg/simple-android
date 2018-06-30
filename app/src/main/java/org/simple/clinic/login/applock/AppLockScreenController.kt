package org.simple.clinic.login.applock

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.login.applock.PasswordHasher.ComparisonResult.DIFFERENT
import org.simple.clinic.login.applock.PasswordHasher.ComparisonResult.SAME
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = AppLockScreen
typealias UiChange = (Ui) -> Unit

class AppLockScreenController @Inject constructor(
    val userSession: UserSession,
    val passwordHasher: PasswordHasher
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.mergeArray(
        populatePhoneNumber(replayedEvents),
        resetValidationError(replayedEvents),
        pinValidations(replayedEvents),
        backClicks(replayedEvents))
  }

  private fun populatePhoneNumber(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockScreenCreated>()
        .flatMap {
          userSession.loggedInUser()
              .map { (it as Just).value }
              .map { it.phoneNumber }
        }
        .map { { ui: Ui -> ui.showPhoneNumber(it) } }
  }

  private fun resetValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockScreenPinTextChanged>()
        .map { { ui: Ui -> ui.hideIncorrectPinError() } }
  }

  private fun pinValidations(events: Observable<UiEvent>): Observable<UiChange> {
    val pinTextChanges = events
        .ofType<AppLockScreenPinTextChanged>()
        .map { it.pin }

    return events
        .ofType<AppLockScreenSubmitClicked>()
        .withLatestFrom(pinTextChanges)
        .flatMapSingle { (_, enteredPin) ->
          userSession.loggedInUser()
              .map { (it as Just).value }
              .map { it.pinDigest }
              .firstOrError()
              .flatMap { pinDigest -> passwordHasher.compare(pinDigest, enteredPin) }
        }
        .map {
          when (it) {
            SAME -> { ui: Ui -> ui.restorePreviousScreen() }
            DIFFERENT -> { ui: Ui -> ui.showIncorrectPinError() }
          }
        }
  }

  private fun backClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockScreenBackClicked>()
        .map { { ui: Ui -> ui.exitApp() } }
  }
}
