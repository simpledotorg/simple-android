package org.simple.clinic.sync.indicator

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
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
import org.simple.clinic.util.ResolvedError
import org.simple.clinic.util.ResolvedError.NetworkRelated
import org.simple.clinic.util.ResolvedError.ServerError
import org.simple.clinic.util.ResolvedError.Unauthenticated
import org.simple.clinic.util.ResolvedError.Unexpected
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class SyncIndicatorUpdate : Update<SyncIndicatorModel, SyncIndicatorEvent, SyncIndicatorEffect> {
  override fun update(model: SyncIndicatorModel, event: SyncIndicatorEvent):
      Next<SyncIndicatorModel, SyncIndicatorEffect> {

    return when (event) {
      is LastSyncedStateFetched -> next(model.lastSyncedStateChanged(event.lastSyncState), FetchDataForSyncIndicatorState)
      is DataForSyncIndicatorStateFetched -> updateSyncIndicatorState(model, event.currentTime, event.syncIndicatorFailureThreshold)
      is IncrementTimerTick -> incrementTimer(model, event)
      is DataSyncErrorReceived -> handleDataSyncErrors(event.errorType)
      SyncIndicatorViewClicked -> syncIndicatorClicked(model.lastSyncedState)
      is PendingSyncRecordsStateFetched -> markStatusAsPending(event, model)
    }
  }

  private fun markStatusAsPending(
      event: PendingSyncRecordsStateFetched,
      model: SyncIndicatorModel
  ): Next<SyncIndicatorModel, SyncIndicatorEffect> {
    return if (event.isSyncPending) {
      next(model.syncIndicatorStateChanged(SyncPending))
    } else {
      noChange()
    }
  }

  private fun syncIndicatorClicked(lastSyncedState: LastSyncedState?): Next<SyncIndicatorModel, SyncIndicatorEffect> {
    val isNotSyncingCurrently = lastSyncedState != null && lastSyncedState.lastSyncProgress != SYNCING
    val hasNotSyncedBefore = lastSyncedState != null && lastSyncedState.lastSyncProgress == null

    return if (hasNotSyncedBefore || isNotSyncingCurrently) {
      dispatch(InitiateDataSync)
    } else {
      noChange()
    }
  }

  private fun handleDataSyncErrors(errorType: ResolvedError): Next<SyncIndicatorModel, SyncIndicatorEffect> {
    return when (errorType) {
      is NetworkRelated, is Unexpected, is ServerError -> dispatch(ShowDataSyncErrorDialog(errorType))
      is Unauthenticated -> noChange()
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
      SUCCESS, FAILURE -> calculateSyncIndicatorState(model.lastSyncedState, failureThreshold, currentTime)
      SYNCING -> Syncing
    }

    return when (syncIndicatorState) {
      is Synced -> next(
          model.syncIndicatorStateChanged(syncIndicatorState),
          StartSyncedStateTimer(Duration.of(1, ChronoUnit.MINUTES))
      )
      else -> next(model.syncIndicatorStateChanged(syncIndicatorState))
    }
  }

  private fun calculateSyncIndicatorState(
      syncState: LastSyncedState,
      maxFailureThresholdSinceLastSync: Duration,
      currentTime: Instant
  ): SyncIndicatorState {
    val lastSucceededSyncTimestamp = syncState.lastSyncSucceededAt ?: return SyncPending

    val timeSinceLastSync = Duration.between(lastSucceededSyncTimestamp, currentTime)
    val mostFrequentSyncIntervalDuration = SyncInterval.mostFrequent()

    // This check is added for cases where the device time is changed to be in the future.
    val syncHappenedInTheFuture = timeSinceLastSync.isNegative

    val hasLastSyncTimeExceededFailureThreshold = timeSinceLastSync > maxFailureThresholdSinceLastSync
    val isLastFrequentSyncPending = timeSinceLastSync > mostFrequentSyncIntervalDuration

    return when {
      hasLastSyncTimeExceededFailureThreshold -> ConnectToSync
      isLastFrequentSyncPending -> SyncPending
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
