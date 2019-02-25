package org.simple.clinic.sync.indicator

import android.annotation.SuppressLint
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.di.AppScope
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncProgress.FAILURE
import org.simple.clinic.sync.SyncProgress.SUCCESS
import org.simple.clinic.sync.SyncProgress.SYNCING
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Instant
import javax.inject.Inject

@AppScope
class SyncIndicatorStatusCalculator @Inject constructor(
    private val dataSync: DataSync,
    private val utcClock: UtcClock,
    private val lastSyncProgress: Preference<LastSyncedState>
) {

  @SuppressLint("CheckResult")
  fun updateSyncResults() {
    val syncResultsStream = dataSync
        .streamSyncResults()
        .distinctUntilChanged()

    syncResultsStream
        .subscribeOn(io())
        .filter { (syncGroup, _) -> syncGroup == SyncGroup.FREQUENT }
        .map { (_, progress) ->
          val updatedState = lastSyncProgress.get().copy(lastSyncProgress = progress)
          when (progress) {
            SUCCESS -> updatedState.copy(lastSyncSucceededAt = Instant.now(utcClock))
            FAILURE, SYNCING -> updatedState
          }
        }
        .subscribe { lastSyncProgress.set(it) }
  }
}
