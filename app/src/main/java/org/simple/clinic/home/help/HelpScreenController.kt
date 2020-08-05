package org.simple.clinic.home.help

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.help.HelpPullResult
import org.simple.clinic.help.HelpRepository
import org.simple.clinic.help.HelpSync
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.filterNotPresent
import org.simple.clinic.widgets.ScreenCreated
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

    return Observable.mergeArray(
        toggleHelpView(replayedEvents),
        syncHelp(replayedEvents)
    )
  }

  private fun toggleHelpView(events: Observable<UiEvent>): Observable<UiChange> {
    val helpContentStream = events
        .ofType<ScreenCreated>()
        .flatMap { repository.helpContentText() }
        .replay()
        .refCount()

    val showHelp = helpContentStream
        .extractIfPresent()
        .map { helpContent -> { ui: Ui -> ui.showHelp(helpContent) } }

    val showEmptyView = helpContentStream
        .filterNotPresent()
        .map { Ui::showNoHelpAvailable }

    return showHelp.mergeWith(showEmptyView)
  }

  private fun syncHelp(events: Observable<UiEvent>): Observable<UiChange> {
    val helpSyncResultsStream = events
        .ofType<HelpScreenTryAgainClicked>()
        .flatMapSingle { sync.pullWithResult() }
        .replay()
        .refCount()

    val showNetworkError = helpSyncResultsStream
        .ofType<HelpPullResult.NetworkError>()
        .map {
          { ui: Ui ->
            ui.showNoHelpAvailable()
            ui.showNetworkErrorMessage()
          }
        }

    val showUnexpectedError = helpSyncResultsStream
        .ofType<HelpPullResult.OtherError>()
        .map {
          { ui: Ui ->
            ui.showNoHelpAvailable()
            ui.showUnexpectedErrorMessage()
          }
        }

    return Observable.merge(showNetworkError, showUnexpectedError)
  }
}
