package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.combineLatest
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SyncGroup.FREQUENT
import org.simple.clinic.sync.SyncInterval
import org.simple.clinic.sync.SyncProgress.FAILURE
import org.simple.clinic.sync.SyncProgress.SUCCESS
import org.simple.clinic.sync.SyncProgress.SYNCING
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.sync.indicator.SyncIndicatorState.ConnectToSync
import org.simple.clinic.sync.indicator.SyncIndicatorState.SyncPending
import org.simple.clinic.sync.indicator.SyncIndicatorState.Synced
import org.simple.clinic.sync.indicator.SyncIndicatorState.Syncing
import org.simple.clinic.util.ResolvedError
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

typealias Ui = SyncIndicatorView
typealias UiChange = (Ui) -> Unit

class SyncIndicatorViewController @Inject constructor(
    private val lastSyncState: Preference<LastSyncedState>,
    private val utcClock: UtcClock,
    private val configProvider: Observable<SyncIndicatorConfig>,
    private val dataSync: DataSync,
    @Named("frequently_syncing_repositories") private val frequentlySyncingRepositories: ArrayList<SynceableRepository<*, *>>
) : ObservableTransformer<UiEvent, UiChange> {

  private val errorTypesToShowErrorFor = setOf(
      ResolvedError.NetworkRelated::class,
      ResolvedError.Unexpected::class
  )

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.merge(
        updateIndicatorView(replayedEvents),
        startSync(replayedEvents),
        showPendingSyncStatus(replayedEvents))
  }

  private fun updateIndicatorView(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreated = events.ofType<SyncIndicatorViewCreated>()
    val lastSyncedStateStream = lastSyncState
        .asObservable()
        .distinctUntilChanged()

    val showDefaultSyncIndicatorState = lastSyncedStateStream
        .filter { it.lastSyncProgress == null }
        .map { { ui: Ui -> ui.updateState(SyncPending) } }

    val syncProgress = lastSyncedStateStream.filter { it.lastSyncProgress != null }

    val showSyncIndicatorState = Observables
        .combineLatest(screenCreated, syncProgress, configProvider)
        { _, stateStream, config ->
          when (stateStream.lastSyncProgress!!) {
            SUCCESS, FAILURE -> syncIndicatorState(stateStream, config.syncFailureThreshold)
            SYNCING -> Syncing
          }
        }
        .switchMap {
          if (it is Synced) {
            val durationSince = it.durationSince
            Observable.interval(1, TimeUnit.MINUTES)
                .map { min -> { ui: Ui -> ui.updateState(Synced(durationSince.plusMinutes(min))) } }
                .startWith { ui: Ui -> ui.updateState(Synced(durationSince)) }
          } else {
            Observable.just { ui: Ui -> ui.updateState(it) }
          }
        }

    return showSyncIndicatorState.mergeWith(showDefaultSyncIndicatorState)
  }

  private fun syncIndicatorState(syncState: LastSyncedState, maxIntervalSinceLastSync: Duration): SyncIndicatorState {
    val timestamp = syncState.lastSyncSucceededAt ?: return SyncPending

    val now = Instant.now(utcClock)
    val timeSinceLastSync = Duration.between(timestamp, now)
    val mostFrequentSyncInterval = enumValues<SyncInterval>()
        .map { it.frequency }
        .min()!!

    val syncHappenedInTheFuture = timeSinceLastSync.isNegative

    return when {
      timeSinceLastSync > maxIntervalSinceLastSync -> ConnectToSync
      timeSinceLastSync > mostFrequentSyncInterval -> SyncPending
      syncHappenedInTheFuture -> SyncPending
      else -> when (syncState.lastSyncProgress!!) {
        SUCCESS -> Synced(timeSinceLastSync)
        FAILURE -> SyncPending
        SYNCING -> Syncing
      }
    }
  }

  private fun startSync(events: Observable<UiEvent>): Observable<UiChange> {
    val errorsStream = {
      dataSync
          .streamSyncErrors()
          .take(1)
          .filter { error -> error::class in errorTypesToShowErrorFor }
          .map { { ui: Ui -> ui.showErrorDialog(it) } }
    }

    val lastSyncedStateStream = lastSyncState
        .asObservable()
        .distinctUntilChanged()

    val syncStream = {
      Timber.tag("SyncWork").i("Sync from sync indicator")
      dataSync
          .sync(FREQUENT)
          .toObservable<UiChange>()
    }

    return events
        .ofType<SyncIndicatorViewClicked>()
        .withLatestFrom(lastSyncedStateStream)
        .filter { (_, lastSyncedState) ->
          lastSyncedState.lastSyncProgress == null || lastSyncedState.lastSyncProgress != SYNCING
        }
        .switchMap { syncStream().mergeWith(errorsStream()) }
  }

  private fun showPendingSyncStatus(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreates = events.ofType<SyncIndicatorViewCreated>()

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
