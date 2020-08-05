package org.simple.clinic.home.help

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.help.HelpRepository
import org.simple.clinic.help.HelpSync
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = HelpScreenUi
typealias UiChange = (Ui) -> Unit

class HelpScreenController @Inject constructor(
    private val repository: HelpRepository,
    private val sync: HelpSync
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.never()
  }
}
