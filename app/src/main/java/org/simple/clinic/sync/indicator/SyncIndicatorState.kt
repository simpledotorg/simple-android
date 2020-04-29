package org.simple.clinic.sync.indicator

import org.threeten.bp.Duration

sealed class SyncIndicatorState {
  object ConnectToSync : SyncIndicatorState()
  data class Synced(val durationSince: Duration) : SyncIndicatorState()
  object SyncPending : SyncIndicatorState()
  object Syncing : SyncIndicatorState()
}
