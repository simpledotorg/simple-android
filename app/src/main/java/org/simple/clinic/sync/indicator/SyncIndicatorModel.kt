package org.simple.clinic.sync.indicator

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.indicator.SyncIndicatorState.Synced
import java.time.Duration

@Parcelize
data class SyncIndicatorModel(
    val lastSyncedState: LastSyncedState?,
    val syncIndicatorState: SyncIndicatorState?
) : Parcelable {

  companion object {
    fun create(): SyncIndicatorModel {
      return SyncIndicatorModel(null, null)
    }
  }

  fun lastSyncedStateChanged(newState: LastSyncedState): SyncIndicatorModel {
    return copy(lastSyncedState = newState)
  }

  fun syncIndicatorStateChanged(newState: SyncIndicatorState): SyncIndicatorModel {
    return copy(syncIndicatorState = newState)
  }

  fun syncIndicatorSyncedTimerIncremented(duration: Duration): SyncIndicatorModel {
    val syncedState = syncIndicatorState as Synced
    val incrementedSyncedState = syncedState.incrementDuration(duration)
    return copy(syncIndicatorState = incrementedSyncedState)
  }
}
