package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.di.AppScope
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncProgress
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

@AppScope
class SyncIndicatorStatusCalculator @Inject constructor(
    dataSync: DataSync,
    clock: Clock,
    @Named("last_frequent_sync_succeeded_timestamp") private val lastSyncSuccessTimestamp: Preference<Optional<Instant>>,
    @Named("last_frequent_sync_result") private val lastSyncProgress: Preference<Optional<SyncProgress>>
) {
  init {
    saveSyncResults(dataSync, clock)
  }

  private fun saveSyncResults(dataSync: DataSync, clock: Clock) {
    dataSync
        .streamSyncResults()
        .distinctUntilChanged()
        .subscribeOn(io())
        .filter { (syncGroup, _) -> syncGroup == SyncGroup.FREQUENT }
        .doOnNext { (_, result) -> lastSyncProgress.set(Just(result)) }
        .filter { (_, result) -> result == SyncProgress.SUCCESS }
        .map { lastSyncSuccessTimestamp.set(Just(Instant.now(clock))) }
        .subscribe()
  }
}
