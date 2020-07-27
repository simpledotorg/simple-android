package org.simple.clinic.login.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = LoginPinScreenUi
typealias UiChange = (Ui) -> Unit

class LoginPinScreenController @Inject constructor(
    private val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.mergeArray(
        backClicks(replayedEvents)
    )
  }

  private fun backClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinBackClicked>()
        .doOnNext { userSession.clearOngoingLoginEntry() }
        .flatMap { Observable.just(Ui::goBackToRegistrationScreen) }
  }
}
