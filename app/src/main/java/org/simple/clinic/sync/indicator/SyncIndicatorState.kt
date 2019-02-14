package org.simple.clinic.sync.indicator

sealed class SyncIndicatorState {
  object ConnectToSync : SyncIndicatorState()
  object Synced : SyncIndicatorState()
  object SyncPending : SyncIndicatorState()
  object Syncing : SyncIndicatorState()
}
