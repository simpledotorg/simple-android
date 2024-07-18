package org.simple.clinic.sync.indicator

import org.simple.clinic.util.ResolvedError
import java.time.Duration

sealed class SyncIndicatorEffect

data object FetchLastSyncedStatus : SyncIndicatorEffect()

data object FetchDataForSyncIndicatorState : SyncIndicatorEffect()

data class StartSyncedStateTimer(val timerDuration: Duration) : SyncIndicatorEffect()

data object InitiateDataSync : SyncIndicatorEffect()

data class ShowDataSyncErrorDialog(val errorType: ResolvedError) : SyncIndicatorEffect()

data object FetchPendingSyncRecordsState : SyncIndicatorEffect()
