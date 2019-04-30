package org.simple.clinic.login.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.login.LoginResult
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = LoginPinScreen
typealias UiChange = (Ui) -> Unit

class LoginPinScreenController @Inject constructor(
    private val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.merge(
        screenSetups(replayedEvents),
        submitClicks(replayedEvents),
        backClicks(replayedEvents))
  }

  private fun screenSetups(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinScreenCreated>()
        .flatMapSingle { _ ->
          userSession.ongoingLoginEntry()
              .map { { ui: Ui -> ui.showPhoneNumber(it.phoneNumber) } }
        }
  }

  private fun submitClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<LoginPinAuthenticated>()
        .map { it.pin }
        .flatMapSingle { pin ->
          val uiChanges = userSession.requestLoginOtp()
              .map { result ->
                when (result) {
                  is LoginResult.Success -> { ui: Ui -> ui.openHomeScreen() }
                  is LoginResult.NetworkError -> { ui: Ui -> ui.showNetworkError() }
                  else -> { ui: Ui -> ui.showUnexpectedError() }
                }
              }
              // This handles the case where listening for SMS fails.
              .onErrorReturn { { ui: Ui -> ui.showUnexpectedError() } }

          userSession.ongoingLoginEntry()
              .map { entry -> entry.copy(pin = pin) }
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
