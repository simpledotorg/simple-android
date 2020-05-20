package org.simple.clinic.sync.indicator

import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.widgets.UiEvent

sealed class SyncIndicatorEvent : UiEvent

data class LastSyncedStateFetched(val lastSyncState: LastSyncedState) : SyncIndicatorEvent()
