package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.disposables.Disposable
import org.simple.clinic.di.AppScope
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SyncTag.FREQUENT
import org.simple.clinic.sync.SyncProgress
import org.simple.clinic.sync.SyncProgress.FAILURE
import org.simple.clinic.sync.SyncProgress.SUCCESS
import org.simple.clinic.sync.SyncProgress.SYNCING
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.annotation.CheckReturnValue
import javax.inject.Inject

@AppScope
class SyncIndicatorStatusCalculator @Inject constructor(
    private val dataSync: DataSync,
    private val utcClock: UtcClock,
    private val lastSyncedStatePreference: Preference<LastSyncedState>,
    private val schedulersProvider: SchedulersProvider
) {

  @CheckReturnValue
  fun updateSyncResults(): Disposable {
    val syncResultsStream = dataSync
        .streamSyncResults()
        .distinctUntilChanged()

    return syncResultsStream
        .subscribeOn(schedulersProvider.io())
        .filter { it.syncTag == FREQUENT }
        .map { it.syncProgress }
        .map(this::updateLastSyncedState)
        .subscribe(lastSyncedStatePreference::set)
  }

  private fun updateLastSyncedState(progress: SyncProgress): LastSyncedState {
    return with(lastSyncedStatePreference.get()) {
      when (progress) {
        SYNCING -> syncStarted()
        SUCCESS -> syncedSuccessfully(utcClock)
        FAILURE -> syncFailed()
      }
    }
  }
}
