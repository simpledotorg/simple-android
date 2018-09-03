package org.simple.clinic.login.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.login.LoginConfig
import org.simple.clinic.login.LoginOtpSmsListener
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.applock.PasswordHasher
import org.simple.clinic.login.applock.PasswordHasher.ComparisonResult.DIFFERENT
import org.simple.clinic.login.applock.PasswordHasher.ComparisonResult.SAME
import org.simple.clinic.sync.SyncScheduler
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = LoginPinScreen
typealias UiChange = (Ui) -> Unit

class LoginPinScreenController @Inject constructor(
    private val userSession: UserSession,
    private val syncScheduler: SyncScheduler,
    private val loginConfig: Single<LoginConfig>,
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

  private fun submitClicks(events: Observable<UiEvent>) =
      loginConfig.flatMapObservable { (isOtpLoginFlowEnabled) ->
        if (isOtpLoginFlowEnabled) loginFlowV2(events) else loginFlowV1(events)
      }

  private fun resetPinValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    val pinTextChanges = events
        .ofType<PinTextChanged>()

    val isOtpLoginFlowEnabled = loginConfig
        .map { it.isOtpLoginFlowEnabled }
        .toObservable()

    return Observables.combineLatest(pinTextChanges, isOtpLoginFlowEnabled)
        .map { (_, isOtpLoginFlowEnabled) ->
          when {
            isOtpLoginFlowEnabled -> { ui: Ui -> ui.hideError() }
            else -> { _: Ui -> }
          }
        }
  }

  private fun loginFlowV1(events: Observable<UiEvent>): Observable<UiChange> {
    val pinChanges = events.ofType<PinTextChanged>()
        .map { it.pin }

    val otpReceived = events.ofType<LoginPinOtpReceived>()
        .map { it.otp }

    return events.ofType<PinSubmitClicked>()
        .withLatestFrom(pinChanges) { _, pin -> pin }
        .withLatestFrom(otpReceived)
        .flatMap { (enteredPin, otp) ->
          val cachedLogin = userSession.loginWithOtp(otp)
              .cache()
              .toObservable()

          val loginResultUiChange = cachedLogin
              .map {
                when (it) {
                  is LoginResult.Success -> { ui: Ui -> ui.openHomeScreen() }
                  is LoginResult.NetworkError -> { ui: Ui -> ui.showNetworkError() }
                  is LoginResult.ServerError -> { ui: Ui -> ui.showServerError(it.error) }
                  is LoginResult.UnexpectedError -> { ui: Ui -> ui.showUnexpectedError() }
                }
              }
              .startWith { ui: Ui -> ui.showProgressBar() }


          val syncRemainingData = cachedLogin
              .filter { it is LoginResult.Success }
              .doOnNext {
                syncScheduler.syncImmediately()
                    .subscribeOn(io())
                    .subscribe()
              }
              .flatMap { Observable.empty<UiChange>() }

          userSession.ongoingLoginEntry()
              .map { entry -> entry.copy(pin = enteredPin) }
              .flatMapCompletable { userSession.saveOngoingLoginEntry(it) }
              .andThen(Observable.merge(
                  loginResultUiChange,
                  syncRemainingData
              ))
        }
  }

  private fun loginFlowV2(events: Observable<UiEvent>): Observable<UiChange> {
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

  private fun backClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinBackClicked>()
        .flatMap {
          userSession.clearLoggedInUser()
              .andThen(userSession.clearOngoingLoginEntry())
              .andThen(Observable.just({ ui: Ui -> ui.goBackToRegistrationScreen() }))
        }
  }
}
