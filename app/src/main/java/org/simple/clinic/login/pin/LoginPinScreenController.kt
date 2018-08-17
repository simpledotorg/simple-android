package org.simple.clinic.login.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.login.LoginConfig
import org.simple.clinic.login.LoginResult
import org.simple.clinic.sms.SmsReadResult
import org.simple.clinic.sms.SmsReader
import org.simple.clinic.sync.SyncScheduler
import org.simple.clinic.user.LoggedInUser
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = LoginPinScreen
typealias UiChange = (Ui) -> Unit

class LoginPinScreenController @Inject constructor(
    private val userSession: UserSession,
    private val syncScheduler: SyncScheduler,
    private val smsReader: SmsReader,
    private val loginConfig: Single<LoginConfig>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.merge(
        screenSetups(replayedEvents),
        submitClicks(replayedEvents),
        backClicks(replayedEvents))
  }

  private fun screenSetups(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinScreenCreated>()
        .flatMapSingle {
          userSession.ongoingLoginEntry()
              .map { { ui: Ui -> ui.showPhoneNumber(it.phoneNumber) } }
        }
  }

  private fun submitClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val pinChanges = events.ofType<PinTextChanged>()
        .map { it.pin }

    val otpReceived = events.ofType<LoginPinOtpReceived>()
        .map { it.otp }

    return events.ofType<PinSubmitClicked>()
        .withLatestFrom(pinChanges) { _, pin -> pin }
        .withLatestFrom(otpReceived)
        .flatMap { (enteredPin, _) ->
          val cachedLogin = userSession.requestLoginOtp()
              .cache()
              .toObservable()

          val loginResultUiChange = cachedLogin
              .map {
                when (it) {
                  is LoginResult.Success -> { ui: Ui ->
                    // No-Op for now
                  }
                  is LoginResult.NetworkError -> { ui: Ui -> ui.showNetworkError() }
                  is LoginResult.ServerError -> { ui: Ui -> ui.showServerError(it.error) }
                  is LoginResult.UnexpectedError -> { ui: Ui -> ui.showUnexpectedError() }
                }
              }

          val loginProgressUiChanges = cachedLogin
              .map { { ui: Ui -> ui.hideProgressBar() } }
              .startWith { ui: Ui -> ui.showProgressBar() }

          val syncRemainingData = cachedLogin
              .filter { it is LoginResult.Success }
              .doOnNext {
                // Stop sync for now since we need to validate otp later and then do sync
                syncScheduler.syncImmediately()
                    .subscribeOn(io())
                //                     .subscribe()
              }
              .flatMap { Observable.empty<UiChange>() }

          val readSms = cachedLogin
              .filter { it is LoginResult.Success }
              .flatMap { smsReader.waitForSms() }
              .flatMapSingle {
                when (it) {
                  is SmsReadResult.Success -> userSession.validateOtp(it.message)
                  is SmsReadResult.Error -> {
                    Single.error<LoggedInUser>(it.cause ?: RuntimeException())
                  }
                }
              }
              .map { { ui: Ui -> ui.showUnexpectedError() } }
              .onErrorReturn { { ui: Ui -> ui.showUnexpectedError() } }

          userSession.ongoingLoginEntry()
              .map { entry -> entry.copy(pin = enteredPin) }
              .flatMapCompletable { userSession.saveOngoingLoginEntry(it) }
              .andThen(Observable.merge(
                  loginProgressUiChanges,
                  loginResultUiChange,
                  syncRemainingData,
                  readSms
              ))
        }
  }

  private fun backClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinBackClicked>()
        .map { { ui: Ui -> ui.goBackToLoginPhoneScreen() } }
  }
}
