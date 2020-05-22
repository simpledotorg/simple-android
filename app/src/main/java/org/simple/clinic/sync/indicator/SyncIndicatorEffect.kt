package org.simple.clinic.sync.indicator

import java.util.concurrent.TimeUnit

sealed class SyncIndicatorEffect

object FetchLastSyncedStatus : SyncIndicatorEffect()

object FetchDataForSyncIndicatorState : SyncIndicatorEffect()

//TODO: Replace the two arguments with a Duration instead
data class StartSyncedStateTimer(
    val intervalAmount: Long,
    val timeUnit: TimeUnit
) : SyncIndicatorEffect()
