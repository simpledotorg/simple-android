package org.simple.clinic.sync.indicator

sealed class SyncIndicatorEffect

object FetchLastSyncedStatus : SyncIndicatorEffect()

object FetchDataForSyncIndicatorState : SyncIndicatorEffect()
