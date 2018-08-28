package org.simple.clinic.home.overdue

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.widgets.UiEvent

typealias Ui = OverdueScreen
typealias UiChange = (Ui) -> Unit

class OverdueScreenController : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(upstream: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = upstream.replay().refCount()

    return screenSetup(replayedEvents)
  }

  private fun screenSetup(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<OverdueScreenCreated>()
        .map { { ui: Ui -> ui.setupOverdueList() } }
  }
}
