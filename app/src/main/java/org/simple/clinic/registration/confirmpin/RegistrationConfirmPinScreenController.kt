package org.simple.clinic.registration.confirmpin

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
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

    return Observable.never()
  }
}
