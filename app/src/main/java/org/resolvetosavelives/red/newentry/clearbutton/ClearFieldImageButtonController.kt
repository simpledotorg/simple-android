package org.resolvetosavelives.red.newentry.clearbutton

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.resolvetosavelives.red.widgets.UiEvent
import javax.inject.Inject

typealias Ui = ClearFieldImageButton
typealias UiChange = (Ui) -> Unit

class ClearFieldImageButtonController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay(1).refCount()

    val textChanges = replayedEvents
        .ofType<CleareableFieldTextChanged>()
        .map { it.text.isNotBlank() }

    val focusChanges = replayedEvents
        .ofType<CleareableFieldFocusChanged>()
        .map { it.hasFocus }

    return Observables.combineLatest(textChanges, focusChanges)
        .map { (hasText, hasFocus) -> hasText.and(hasFocus) }
        .distinctUntilChanged()
        .map { textCanBeCleared -> { ui: Ui -> ui.setVisible(textCanBeCleared) } }
  }
}
