package org.simple.clinic.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val syncs: ArrayList<ModelSync>
) {

  fun schedule(): Completable {
    return Observable.fromIterable(syncs)
        .flatMapSingle { it.syncConfig() }
        .distinct()
        .flatMapSingle(this::createWorkRequest)
        .toList()
        .flatMapCompletable { workRequests ->
          Completable.fromAction {
            workManager.cancelAllWorkByTag(SyncWorker.TAG)
            workManager.enqueue(workRequests)
          }
        }
  }

  private fun createWorkRequest(syncConfig: SyncConfig): Single<WorkRequest> {
    return Single.fromCallable {
      val constraints = Constraints.Builder()
          .setRequiredNetworkType(NetworkType.CONNECTED)
          .setRequiresBatteryNotLow(true)
          .build()

      PeriodicWorkRequestBuilder<SyncWorker>(syncConfig.frequency.toMinutes(), TimeUnit.MINUTES)
          .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, syncConfig.backOffDelay.toMinutes(), TimeUnit.MINUTES)
          .setConstraints(constraints)
          .setInputData(SyncWorker.createWorkDataForSyncConfig(syncConfig))
          .addTag(SyncWorker.TAG)
          .build()
    }
  }

  fun cancelAll() {
    workManager.cancelAllWorkByTag(SyncWorker.TAG)
  }
}
