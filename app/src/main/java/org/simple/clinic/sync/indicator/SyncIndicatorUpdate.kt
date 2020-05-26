package org.simple.clinic.sync.indicator

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SyncInterval
import org.simple.clinic.sync.SyncProgress.FAILURE
import org.simple.clinic.sync.SyncProgress.SUCCESS
import org.simple.clinic.sync.SyncProgress.SYNCING
import org.simple.clinic.sync.indicator.SyncIndicatorState.ConnectToSync
import org.simple.clinic.sync.indicator.SyncIndicatorState.SyncPending
import org.simple.clinic.sync.indicator.SyncIndicatorState.Synced
import org.simple.clinic.sync.indicator.SyncIndicatorState.Syncing
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class SyncIndicatorUpdate : Update<SyncIndicatorModel, SyncIndicatorEvent, SyncIndicatorEffect> {
  override fun update(model: SyncIndicatorModel, event: SyncIndicatorEvent):
      Next<SyncIndicatorModel, SyncIndicatorEffect> {

    return when (event) {
      is LastSyncedStateFetched -> next(model.lastSyncedStateChanged(event.lastSyncState), FetchDataForSyncIndicatorState)
      is DataForSyncIndicatorStateFetched -> updateSyncIndicatorState(model, event.currentTime, event.syncIndicatorFailureThreshold)
      is IncrementTimerTick -> incrementTimer(model, event)
      is DataSyncErrorReceived -> noChange()
    }
  }

  private fun incrementTimer(
      model: SyncIndicatorModel,
      event: IncrementTimerTick
  ): Next<SyncIndicatorModel, SyncIndicatorEffect> {
    return if (model.syncIndicatorState is Synced) {
      val incrementDuration = Duration.of(event.minutes, ChronoUnit.MINUTES)
      next(model.syncIndicatorSyncedTimerIncremented(incrementDuration))
    } else {
      noChange()
    }
  }

  private fun updateSyncIndicatorState(
      model: SyncIndicatorModel,
      currentTime: Instant,
      failureThreshold: Duration
  ): Next<SyncIndicatorModel, SyncIndicatorEffect> {
    if (model.lastSyncedState == null || model.lastSyncedState.isEmpty()) {
      return noChange()
    }

    val syncIndicatorState = when (model.lastSyncedState.lastSyncProgress!!) {
      SUCCESS, FAILURE -> syncIndicatorState(model.lastSyncedState, failureThreshold, currentTime)
      SYNCING -> Syncing
    }

    return when (syncIndicatorState) {
      is Synced -> next(model.syncIndicatorStateChanged(syncIndicatorState), StartSyncedStateTimer(
          intervalAmount = 1,
          timeUnit = TimeUnit.MINUTES
      ))
      else -> next(model.syncIndicatorStateChanged(syncIndicatorState))
    }
  }

  //TODO: Refactor this method after migration to Mobius is complete.
  // This method is copied as is from `SyncIndicatorController` for migration purpose.
  private fun syncIndicatorState(
      syncState: LastSyncedState,
      maxIntervalSinceLastSync: Duration,
      currentTime: Instant
  ): SyncIndicatorState {
    //TODO: Convert to use `if`
    val timestamp = syncState.lastSyncSucceededAt ?: return SyncPending

    val timeSinceLastSync = Duration.between(timestamp, currentTime)
    val mostFrequentSyncInterval = enumValues<SyncInterval>()
        .map { it.frequency }
        .min()!!

    // This check is added for cases where the device time is changed to be in the future.
    val syncHappenedInTheFuture = timeSinceLastSync.isNegative

    return when {
    //TODO: Extract conditions to variables with relevant names
      timeSinceLastSync > maxIntervalSinceLastSync -> ConnectToSync
      timeSinceLastSync > mostFrequentSyncInterval -> SyncPending
      syncHappenedInTheFuture -> SyncPending
      else -> syncStateFromProgress(syncState, timeSinceLastSync)
    }
  }

  private fun syncStateFromProgress(
      syncState: LastSyncedState,
      timeSinceLastSync: Duration
  ): SyncIndicatorState {
    return when (syncState.lastSyncProgress!!) {
      SUCCESS -> Synced(timeSinceLastSync)
      FAILURE -> SyncPending
      SYNCING -> Syncing
    }
  }

}
