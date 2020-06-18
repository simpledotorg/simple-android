package org.simple.clinic.registration.confirmpin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationConfirmPinUi
typealias UiChange = (Ui) -> Unit

class RegistrationConfirmPinScreenController @Inject constructor(
    private val userSession: UserSession,
    private val utcClock: UtcClock
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return resetPins(replayedEvents)
  }

  private fun resetPins(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationResetPinClicked>()
        .map { ongoingRegistrationEntry() }
        .map(OngoingRegistrationEntry::resetPin)
        .doOnNext(userSession::saveOngoingRegistrationEntry)
        .map { { ui: Ui -> ui.goBackToPinScreen() } }
  }

  private fun ongoingRegistrationEntry(): OngoingRegistrationEntry = (userSession.ongoingRegistrationEntry() as Just).value
}
