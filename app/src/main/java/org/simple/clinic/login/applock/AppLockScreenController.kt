package org.simple.clinic.login.applock

import com.f2prateek.rx.preferences2.Preference
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
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

typealias Ui = AppLockScreen
typealias UiChange = (Ui) -> Unit

class AppLockScreenController @Inject constructor(
    private val userSession: UserSession,
    private val passwordHasher: PasswordHasher,
    @Named("should_lock_after") private val lockAfterTimestamp: Preference<Instant>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.mergeArray(
        populateFullName(replayedEvents),
        resetValidationError(replayedEvents),
        pinValidations(replayedEvents),
        backClicks(replayedEvents))
  }

  private fun populateFullName(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockScreenCreated>()
        .flatMap {
          userSession.loggedInUser()
              .map { (it as Just).value }
              .map { it.fullName }
        }
        .map { { ui: Ui -> ui.showFullName(it) } }
  }

  private fun resetValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockScreenPinTextChanged>()
        .map {
          { ui: Ui ->
            ui.setIncorrectPinErrorVisible(false)
            Unit
          }
        }
  }

  private fun pinValidations(events: Observable<UiEvent>): Observable<UiChange> {
    val pinTextChanges = events
        .ofType<AppLockScreenPinTextChanged>()
        .map { it.pin }

    return events
        .ofType<AppLockScreenSubmitClicked>()
        .withLatestFrom(pinTextChanges)
        .flatMap { (_, enteredPin) ->
          val cachedPinValidation = userSession.loggedInUser()
              .map { (it as Just).value }
              .map { it.pinDigest }
              .firstOrError()
              .flatMap { pinDigest -> passwordHasher.compare(pinDigest, enteredPin) }

          val validationResultUiChange = cachedPinValidation
              .map {
                when (it) {
                  SAME -> { ui: Ui -> ui.restorePreviousScreen() }
                  DIFFERENT -> { ui: Ui ->
                    ui.setIncorrectPinErrorVisible(true)
                  }
                }
              }
              .toObservable()

          val progressUiChanges = cachedPinValidation
              .filter { it == DIFFERENT }
              .map { { ui: Ui -> ui.setProgressVisible(false) } }
              .toObservable()
              .startWith { ui: Ui -> ui.setProgressVisible(true) }

          val recordLastLock = cachedPinValidation
              .filter { it == SAME }
              .flatMapObservable {
                lockAfterTimestamp.delete()
                Observable.empty<UiChange>()
              }

          Observable.mergeArray(progressUiChanges, recordLastLock, validationResultUiChange)
        }
  }

  private fun backClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockScreenBackClicked>()
        .map { { ui: Ui -> ui.exitApp() } }
  }
}
