package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.sync.SyncProgress
import org.simple.clinic.sync.SyncProgress.FAILURE
import org.simple.clinic.sync.SyncProgress.SUCCESS
import org.simple.clinic.sync.SyncProgress.SYNCING
import org.simple.clinic.sync.indicator.SyncIndicatorState.SyncPending
import org.simple.clinic.sync.indicator.SyncIndicatorState.Synced
import org.simple.clinic.sync.indicator.SyncIndicatorState.Syncing
import org.simple.clinic.util.Optional
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

typealias Ui = SyncIndicatorView
typealias UiChange = (Ui) -> Unit

class SyncIndicatorViewController @Inject constructor(
    @Named("last_frequent_sync_succeeded_timestamp") private val lastSyncSucceededTimestamp: Preference<Optional<Instant>>,
    @Named("last_frequent_sync_result") private val lastSyncGroupResult: Preference<Optional<SyncProgress>>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return updateIndicatorView(replayedEvents)
  }

  private fun updateIndicatorView(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreated = events.ofType<SyncIndicatorViewCreated>()
    val syncResultStream = lastSyncGroupResult.asObservable()
        .distinctUntilChanged()
        .filterAndUnwrapJust()

    return Observables.combineLatest(screenCreated, syncResultStream) { _, result ->
      val state = when (result) {
        SUCCESS -> Synced
        FAILURE -> SyncPending
        SYNCING -> Syncing
      }
      { ui: Ui -> ui.updateState(state) }
    }
  }
}

