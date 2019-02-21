package org.simple.clinic.sync.indicator

sealed class SyncIndicatorState {
  object ConnectToSync : SyncIndicatorState()
  data class Synced(val minAgo: Long) : SyncIndicatorState()
  object SyncPending : SyncIndicatorState()
  object Syncing : SyncIndicatorState()
}
