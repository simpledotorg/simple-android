package org.simple.clinic.sync.indicator

import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.util.ResolvedError
import org.simple.clinic.widgets.UiEvent
import java.time.Duration
import java.time.Instant

sealed class SyncIndicatorEvent : UiEvent

data class LastSyncedStateFetched(val lastSyncState: LastSyncedState) : SyncIndicatorEvent()

data class DataForSyncIndicatorStateFetched(
    val currentTime: Instant,
    val syncIndicatorFailureThreshold: Duration
) : SyncIndicatorEvent()

data class IncrementTimerTick(val minutes: Long) : SyncIndicatorEvent()

data class DataSyncErrorReceived(val errorType: ResolvedError) : SyncIndicatorEvent()

object SyncIndicatorViewClicked : SyncIndicatorEvent() {
  override val analyticsName = "Sync Indicator: View Clicked"
}

data class PendingSyncRecordsStateFetched(val isSyncPending: Boolean) : SyncIndicatorEvent()
