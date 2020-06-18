package org.simple.clinic.registration.confirmpin

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent

typealias Ui = RegistrationConfirmPinUi
typealias UiChange = (Ui) -> Unit

class RegistrationConfirmPinScreenController @AssistedInject constructor(
    private val userSession: UserSession,
    private val utcClock: UtcClock,
    @Assisted private val modelSupplier: () -> RegistrationConfirmPinModel
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(modelSupplier: () -> RegistrationConfirmPinModel): RegistrationConfirmPinScreenController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return resetPins(replayedEvents)
  }

  private fun resetPins(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationResetPinClicked>()
        .map { modelSupplier() }
        .map { it.ongoingRegistrationEntry.resetPin() }
        .map { { ui: Ui -> ui.goBackToPinScreen(it) } }
  }
}
