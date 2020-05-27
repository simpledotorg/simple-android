package org.simple.clinic.sync.indicator

interface SyncIndicatorUi {
  fun updateState(syncState: SyncIndicatorState)
}
