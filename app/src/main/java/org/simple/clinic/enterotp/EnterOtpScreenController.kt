package org.simple.clinic.enterotp

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.login.LoginResult
import org.simple.clinic.sync.SyncScheduler
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = EnterOtpScreen
typealias UiChange = (Ui) -> Unit

private const val OTP_LENGTH = 6

class EnterOtpScreenController @Inject constructor(
    private val userSession: UserSession,
    private val syncScheduler: SyncScheduler
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.merge(
        showPhoneNumberOnStart(replayedEvents),
        handleBackClicks(replayedEvents),
        showOtpValidationErrors(replayedEvents),
        handleOtpSubmitted(replayedEvents)
    )
  }

  private fun showOtpValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    val showIncorrectOtpErrors = events.ofType<EnterOtpSubmitted>()
        .filter { it.otp.length != OTP_LENGTH }
        .map { { ui: Ui -> ui.showIncorrectOtpError() } }

    val hideOtpErrorOnPinTextChanges = events.ofType<EnterOtpTextChanges>()
        .map { { ui: Ui -> ui.hideError() } }

    return showIncorrectOtpErrors.mergeWith(hideOtpErrorOnPinTextChanges)
  }

  private fun showPhoneNumberOnStart(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ScreenCreated>()
        .flatMapSingle { userSession.requireLoggedInUser().firstOrError() }
        .map { user -> { ui: Ui -> ui.showUserPhoneNumber(user.phoneNumber) } }
  }

  private fun handleBackClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<EnterOtpBackClicked>()
        .map { { ui: Ui -> ui.goBack() } }
  }

  private fun handleOtpSubmitted(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<EnterOtpSubmitted>()
        .filter { it.otp.length == OTP_LENGTH }
        .flatMapSingle { userSession.loginWithOtp(it.otp) }
        .map { loginResult ->
          when (loginResult) {
            is LoginResult.Success -> { ui: Ui ->
              startSyncing()
              ui.goBack()
            }
            is LoginResult.UnexpectedError -> { ui: Ui -> ui.showUnexpectedError() }
            is LoginResult.ServerError -> { ui: Ui -> ui.showServerError(loginResult.error) }
            is LoginResult.NetworkError -> { ui: Ui -> ui.showNetworkError() }
          }
        }
  }

  private fun startSyncing() {
    syncScheduler.syncImmediately()
        .subscribeOn(Schedulers.io())
        // Swallow errors
        .onErrorComplete()
        .subscribe()
  }
}
