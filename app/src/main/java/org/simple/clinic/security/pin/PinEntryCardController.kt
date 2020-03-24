package org.simple.clinic.security.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PinEntryUi
typealias UiChange = (Ui) -> Unit

class PinEntryCardController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return Observable.never()
  }
}
