package org.simple.clinic.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import io.reactivex.Completable
import io.reactivex.Observable
import org.simple.clinic.sync.SyncConfigType.Type.Frequent
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val syncs: List<@JvmSuppressWildcards ModelSync>,
    @SyncConfigType(Frequent) private val syncConfig: SyncConfig
) {

  fun schedule(): Completable {
    return Observable
        .fromIterable(syncs)
        .map { it.syncConfig() }
        .distinct { it.syncGroup }
        .map { config -> createWorkRequest(config.syncInterval) to config.syncGroup.name }
        .toList()
        .doOnSuccess { cancelPreviouslyScheduledPeriodicWork() }
        .flatMapCompletable(this::scheduleWorkRequests)
  }

  /*
   * This is meant to cancel the periodic work that was scheduled using the `SyncGroup` enum
   * until enough devices have migrated over to the system where we schedule only one periodic
   * sync
   * TODO vs(2021-07-15): Remove once the single unique sync work has been deployed to enough devices
   **/
  private fun cancelPreviouslyScheduledPeriodicWork() {
    workManager.cancelUniqueWork("FREQUENT")
    workManager.cancelUniqueWork("DAILY")
  }

  private fun scheduleWorkRequests(workRequests: List<Pair<PeriodicWorkRequest, String>>): Completable {
    return Completable.fromAction {

      workRequests.forEach { (request, name) ->
        workManager.enqueueUniquePeriodicWork(name, REPLACE, request)
      }
    }
  }

  private fun createWorkRequest(syncInterval: SyncInterval): PeriodicWorkRequest {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    val syncRepeatIntervalMillis = syncInterval
        .frequency
        .toMillis()
        .coerceAtLeast(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS)
    val syncFlexIntervalMillis = PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS
    val syncBackoffIntervalMillis = syncInterval
        .backOffDelay
        .toMillis()
        .coerceAtLeast(WorkRequest.MIN_BACKOFF_MILLIS)
        .coerceAtMost(WorkRequest.MAX_BACKOFF_MILLIS)

    return PeriodicWorkRequest
        .Builder(
            SyncWorker::class.java,
            syncRepeatIntervalMillis, TimeUnit.MILLISECONDS,
            syncFlexIntervalMillis, TimeUnit.MILLISECONDS
        )
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, syncBackoffIntervalMillis, TimeUnit.MILLISECONDS)
        .setConstraints(constraints)
        .build()
  }
}
