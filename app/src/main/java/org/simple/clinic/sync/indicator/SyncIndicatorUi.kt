package org.simple.clinic.sync.indicator

interface SyncIndicatorUi : SyncIndicatorUiActions {
  fun updateState(syncState: SyncIndicatorState)
}
