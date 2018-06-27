package org.simple.clinic.login.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
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
    val replayedEvents = events.replay().refCount()

    return Observable.merge(
        screenSetups(replayedEvents),
        pinChanges(replayedEvents),
        submitClicks(replayedEvents))
  }

  private fun screenSetups(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinScreenCreated>()
        .flatMapSingle {
          userSession.ongoingLoginEntry()
              .map { { ui: Ui -> ui.showPhoneNumber(it.phoneNumber!!) } }
        }
  }

  private fun pinChanges(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinTextChanged>()
        .map { it.pin.isNotBlank() }
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.enableSubmitButton(it) } }
  }

  private fun submitClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val pinChanges = events.ofType<PinTextChanged>()
        .map { it.pin }

    return events.ofType<PinSubmitClicked>()
        .withLatestFrom(pinChanges) { _, pin -> pin }
        .flatMap { enteredPin ->
          val cachedLogin = userSession.login()
              .cache()
              .toObservable()

          val loginResultUiChange = cachedLogin
              .map {
                when (it) {
                  is LoginResult.Success -> { ui: Ui -> ui.openHomeScreen() }
                  is LoginResult.NetworkError -> { ui: Ui -> ui.showNetworkError() }
                  is LoginResult.ServerError -> { ui: Ui -> ui.showServerError() }
                  is LoginResult.UnexpectedError -> { ui: Ui -> ui.showUnexpectedError() }
                }
              }

          val loginProgressUiChanges = cachedLogin
              .map { { ui: Ui -> ui.hideProgressBar() } }
              .startWith { ui: Ui -> ui.showProgressBar() }

          userSession.ongoingLoginEntry()
              .map { entry -> entry.copy(pin = enteredPin) }
              .flatMapCompletable { userSession.saveOngoingLoginEntry(it) }
              .andThen(loginResultUiChange.mergeWith(loginProgressUiChanges))
        }
  }
}
