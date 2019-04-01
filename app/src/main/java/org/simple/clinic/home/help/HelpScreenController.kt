package org.simple.clinic.home.help

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.help.HelpRepository
import org.simple.clinic.util.None
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = HelpScreen
typealias UiChange = (Ui) -> Unit

class HelpScreenController @Inject constructor(
    private val repository: HelpRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    val helpFileStream = replayedEvents
        .ofType<ScreenCreated>()
        .flatMap { repository.helpFile() }
        .replay()
        .refCount()

    val showHelp = helpFileStream
        .filterAndUnwrapJust()
        .map { it.toURI() }
        .map { helpFileUri -> { ui: Ui -> ui.showHelp(helpFileUri) } }

    val showEmptyView = helpFileStream
        .ofType<None>()
        .map { Ui::showNoHelpAvailable }

    return showHelp.mergeWith(showEmptyView)
  }
}
