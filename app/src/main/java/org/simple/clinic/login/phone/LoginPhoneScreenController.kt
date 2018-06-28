package org.simple.clinic.login.phone

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = LoginPhoneScreen
typealias UiChange = (Ui) -> Unit

class LoginPhoneScreenController @Inject constructor(
    private val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.merge(
        screenSetups(replayedEvents),
        phoneNumberChanges(replayedEvents),
        submitClicks(replayedEvents))
  }

  private fun screenSetups(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PhoneNumberScreenCreated>()
        .map { OngoingLoginEntry(it.otp) }
        .flatMap {
          userSession
              .saveOngoingLoginEntry(it)
              .toObservable<UiChange>()
        }
  }

  private fun phoneNumberChanges(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PhoneNumberTextChanged>()
        .map { it.phoneNumber.isNotBlank() }
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.enableSubmitButton(it) } }
  }

  private fun submitClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val phoneNumberChanges = events.ofType<PhoneNumberTextChanged>()
        .map { it.phoneNumber }

    return events.ofType<PhoneNumberSubmitClicked>()
        .withLatestFrom(phoneNumberChanges)
        .flatMap { (_, enteredPhoneNumber) ->
          userSession.ongoingLoginEntry()
              .map { it.copy(phoneNumber = enteredPhoneNumber) }
              .flatMapCompletable { userSession.saveOngoingLoginEntry(it) }
              .andThen(Observable.just({ ui: Ui -> ui.openLoginPinScreen() }))
        }
  }

}
