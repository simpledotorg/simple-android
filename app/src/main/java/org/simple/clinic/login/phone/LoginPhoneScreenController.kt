package org.simple.clinic.login.phone

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.widgets.UiEvent

typealias Ui = LoginPhoneScreen
typealias UiChange = (Ui) -> Unit

class LoginPhoneScreenController : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return replayedEvents.map { ui: UiEvent -> ui as UiChange }
  }


}
