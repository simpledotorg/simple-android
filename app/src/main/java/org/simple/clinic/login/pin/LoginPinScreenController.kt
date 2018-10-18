package org.simple.clinic.login.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.login.LoginOtpSmsListener
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.applock.PasswordHasher
import org.simple.clinic.login.applock.PasswordHasher.ComparisonResult.DIFFERENT
import org.simple.clinic.login.applock.PasswordHasher.ComparisonResult.SAME
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = LoginPinScreen
typealias UiChange = (Ui) -> Unit

class LoginPinScreenController @Inject constructor(
    private val userSession: UserSession,
    private val loginOtpSmsListener: LoginOtpSmsListener,
    private val passwordHasher: PasswordHasher
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.merge(
        screenSetups(replayedEvents),
        submitClicks(replayedEvents),
        backClicks(replayedEvents),
        resetPinValidationError(replayedEvents))
  }

  private fun screenSetups(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinScreenCreated>()
        .flatMapSingle { _ ->
          userSession.ongoingLoginEntry()
              .map { { ui: Ui -> ui.showPhoneNumber(it.phoneNumber) } }
        }
  }

  private fun submitClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val pinChanges = events.ofType<PinTextChanged>()
        .map { it.pin }

    return events.ofType<PinSubmitClicked>()
        .withLatestFrom(pinChanges) { _, pin -> pin }
        .flatMap { enteredPin ->

          val pinValidation = userSession.loggedInUser()
              .map { (it as Just).value }
              .map { it.pinDigest }
              .firstOrError()
              .flatMap { pinDigest -> passwordHasher.compare(pinDigest, enteredPin) }
              .cache()

          val pinEnteredSuccessfully = pinValidation
              .toObservable()
              .filter { it == SAME }
              .cache()

          val cachedRequestOtp = pinEnteredSuccessfully
              .flatMapSingle {
                loginOtpSmsListener.listenForLoginOtp()
                    // LoginOtpSmsListener depends on a Google Play Services task
                    // which emits the result on the main thread
                    .observeOn(Schedulers.io())
                    .andThen(userSession.requestLoginOtp())
              }
              .cache()

          val showProgressOnPinValidation = pinValidation
              .map {
                when (it) {
                  SAME -> { ui: Ui -> ui.showProgressBar() }
                  DIFFERENT -> { ui: Ui -> ui.showIncorrectPinError() }
                }
              }
              .toObservable()

          val uiChanges = cachedRequestOtp
              .map {
                when (it) {
                  is LoginResult.Success -> { ui: Ui -> ui.openHomeScreen() }
                  is LoginResult.NetworkError -> { ui: Ui -> ui.showNetworkError() }
                  else -> { ui: Ui -> ui.showUnexpectedError() }
                }
              }
              // This handles the case where listening for SMS fails
              .onErrorReturn { { ui: Ui -> ui.showUnexpectedError() } }
              .mergeWith(showProgressOnPinValidation)

          userSession.ongoingLoginEntry()
              .map { entry -> entry.copy(pin = enteredPin) }
              .flatMapCompletable { userSession.saveOngoingLoginEntry(it) }
              .andThen(uiChanges)
        }
  }

  private fun resetPinValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PinTextChanged>()
        .map { { ui: Ui -> ui.hideError() } }
  }

  private fun backClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinBackClicked>()
        .flatMap {
          userSession.clearLoggedInUser()
              .andThen(userSession.clearOngoingLoginEntry())
              .andThen(Observable.just({ ui: Ui -> ui.goBackToRegistrationScreen() }))
        }
  }
}
