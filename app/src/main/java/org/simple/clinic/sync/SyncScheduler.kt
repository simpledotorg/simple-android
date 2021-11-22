package org.simple.clinic.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.sync.SyncConfigType.Type.Frequent
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncScheduler @Inject constructor(
    private val workManager: WorkManager,
    @SyncConfigType(Frequent) private val syncConfig: SyncConfig
) {

  fun schedule(): Completable {
    return Single.just(syncConfig)
        .map { config -> createWorkRequest(config.syncInterval) }
        .doOnSuccess { request -> workManager.enqueueUniquePeriodicWork(syncConfig.name, REPLACE, request) }
        .ignoreElement()
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
