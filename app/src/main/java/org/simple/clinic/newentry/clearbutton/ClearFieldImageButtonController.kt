package org.simple.clinic.newentry.clearbutton

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = ClearFieldImageButton
typealias UiChange = (Ui) -> Unit

class ClearFieldImageButtonController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay(1).refCount()

    val textChanges = replayedEvents
        .ofType<ClearableFieldTextChanged>()
        .map { it.text.isNotBlank() }

    val focusChanges = replayedEvents
        .ofType<ClearableFieldFocusChanged>()
        .map { it.hasFocus }

    return Observables.combineLatest(textChanges, focusChanges)
        .map { (hasText, hasFocus) -> hasText.and(hasFocus) }
        .distinctUntilChanged()
        .map { textCanBeCleared -> { ui: Ui -> ui.setVisible(textCanBeCleared) } }
  }
}
