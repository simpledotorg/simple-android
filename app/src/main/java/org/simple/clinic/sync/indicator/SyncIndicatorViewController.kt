package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.combineLatest
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.sync.indicator.SyncIndicatorState.SyncPending
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject
import javax.inject.Named

typealias Ui = SyncIndicatorUi
typealias UiChange = (Ui) -> Unit

class SyncIndicatorViewController @Inject constructor(
    private val lastSyncState: Preference<LastSyncedState>,
    private val utcClock: UtcClock,
    private val configProvider: Observable<SyncIndicatorConfig>,
    private val dataSync: DataSync,
    @Named("frequently_syncing_repositories") private val frequentlySyncingRepositories: ArrayList<SynceableRepository<*, *>>
) : ObservableTransformer<UiEvent, UiChange> {
  
  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return showPendingSyncStatus(replayedEvents)
  }

  private fun showPendingSyncStatus(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreates = events.ofType<ScreenCreated>()

    return screenCreates
        .flatMap {
          val recordCounts = frequentlySyncingRepositories
              .map { it.pendingSyncRecordCount() }

          recordCounts
              .combineLatest { counts -> counts.any { it > 0 } }
              .filter { isSyncPending -> isSyncPending }
              .distinctUntilChanged()
              .map { { ui: Ui -> ui.updateState(SyncPending) } }
        }
  }
}
