package org.simple.clinic.enterotp

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.login.LoginResult
import org.simple.clinic.user.NewlyVerifiedUser
import org.simple.clinic.user.RequestLoginOtp
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

typealias Ui = EnterOtpScreen
typealias UiChange = (Ui) -> Unit

private const val OTP_LENGTH = 6

class EnterOtpScreenController @Inject constructor(
    private val userSession: UserSession,
    private val requestLoginOtp: RequestLoginOtp
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        showPhoneNumberOnStart(replayedEvents),
        handleBackClicks(replayedEvents),
        showOtpValidationErrors(replayedEvents),
        makeLoginCall(replayedEvents),
        closeScreenOnUserLoginInBackground(replayedEvents),
        resendSms(replayedEvents)
    )
  }

  private fun showOtpValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<EnterOtpSubmitted>()
        .filter { it.otp.length != OTP_LENGTH }
        .map {
          { ui: Ui ->
            ui.showIncorrectOtpError()
            ui.clearPin()
          }
        }
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

  private fun makeLoginCall(events: Observable<UiEvent>): Observable<UiChange> {
    val otpFromSubmitted = events.ofType<EnterOtpSubmitted>()
        .filter { it.otp.length == OTP_LENGTH }
        .map { it.otp }

    val otpFromTextChanges = events.ofType<EnterOtpTextChanges>()
        .filter { it.otp.length == OTP_LENGTH }
        .map { it.otp }

    return Observable.merge(otpFromSubmitted, otpFromTextChanges)
        .flatMap { otp ->
          userSession.loginWithOtp(otp)
              .flatMapObservable { loginResult ->
                Observable.merge(
                    handleLoginResult(loginResult),
                    Observable.just({ ui: Ui -> ui.hideProgress() }))
              }
              .startWith { ui: Ui -> ui.showProgress() }
        }
  }

  private fun handleLoginResult(loginResult: LoginResult): Observable<UiChange> {
    return Single.just(loginResult)
        .map {
          when (it) {
            is LoginResult.Success -> { ui: Ui -> ui.goBack() }
            is LoginResult.NetworkError -> { ui: Ui ->
              ui.showNetworkError()
              ui.clearPin()
            }
            is LoginResult.ServerError -> { ui: Ui ->
              ui.showServerError(it.error)
              ui.clearPin()
            }
            is LoginResult.UnexpectedError -> { ui: Ui ->
              ui.showUnexpectedError()
              ui.clearPin()
            }
          }
        }
        .toObservable()
  }

  private fun closeScreenOnUserLoginInBackground(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ScreenCreated>()
        .flatMap { userSession.loggedInUser() }
        .compose(NewlyVerifiedUser())
        .map { { ui: Ui -> ui.goBack() } }
  }

  private fun resendSms(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<EnterOtpResendSmsClicked>()
        .flatMapSingle { loggedInUserUuid() }
        .flatMap(this::requestLoginOtpForUser)
  }

  private fun requestLoginOtpForUser(userUuid: UUID): Observable<UiChange> {
    val showProgressBeforeRequestingOtp = { ui: Ui ->
      ui.hideError()
      ui.showProgress()
    }

    return requestLoginOtp
        .requestForUser(userUuid)
        .toObservable()
        .map(this::handleRequestLoginOtpResult)
        .startWith(showProgressBeforeRequestingOtp)
  }

  private fun handleRequestLoginOtpResult(result: RequestLoginOtp.Result): UiChange {
    return { ui: Ui ->
      showMessageOnResendLoginSmsResult(ui, result)
      hideProgressAfterRequestingOtp(ui)
    }
  }

  private fun loggedInUserUuid(): Single<UUID> {
    return userSession
        .requireLoggedInUser()
        .firstOrError()
        .map { it.uuid }
  }

  private fun showMessageOnResendLoginSmsResult(ui: Ui, result: RequestLoginOtp.Result) {
    when (result) {
      is RequestLoginOtp.Result.NetworkError -> ui.showNetworkError()
      is RequestLoginOtp.Result.ServerError, is RequestLoginOtp.Result.OtherError -> ui.showUnexpectedError()
      is RequestLoginOtp.Result.Success -> ui.showSmsSentMessage()
    }
  }

  private fun hideProgressAfterRequestingOtp(ui: Ui) {
    ui.hideProgress()
    ui.clearPin()
  }
}
