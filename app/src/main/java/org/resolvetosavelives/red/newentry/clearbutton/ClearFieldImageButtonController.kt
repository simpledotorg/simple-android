package org.resolvetosavelives.red.newentry.clearbutton

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.resolvetosavelives.red.widgets.UiEvent
import javax.inject.Inject

typealias Ui = ClearFieldImageButton
typealias UiChange = (Ui) -> Unit

class ClearFieldImageButtonController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay(1).refCount()

    return replayedEvents
        .ofType<CleareableFieldTextChanged>()
        .map { it.text.isNotBlank() }
        .distinctUntilChanged()
        .map { hasText -> { ui: Ui -> ui.setVisible(hasText) } }
  }
}
