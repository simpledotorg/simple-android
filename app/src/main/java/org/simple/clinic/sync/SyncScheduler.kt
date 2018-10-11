package org.simple.clinic.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val syncConfigProvider: Single<SyncConfig>
) {

  fun schedule(): Completable {
    return syncConfigProvider
        .map { config ->
          val constraints = Constraints.Builder()
              .setRequiredNetworkType(NetworkType.CONNECTED)
              .setRequiresBatteryNotLow(true)
              .build()

          PeriodicWorkRequestBuilder<SyncWorker>(config.frequency.toMillis(), TimeUnit.MILLISECONDS)
              .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
              .setConstraints(constraints)
              .addTag(SyncWorker.TAG)
              .build()
        }
        .flatMapCompletable { request ->
          Completable.fromAction {
            workManager.cancelAllWorkByTag(SyncWorker.TAG)
            workManager.enqueue(request)
          }
        }
  }

  fun syncImmediately(): Completable {
    return Completable.fromAction {
      val worker = OneTimeWorkRequestBuilder<SyncWorker>().build()
      workManager.enqueue(worker)
    }
  }

  fun cancelAll() {
    workManager.cancelAllWorkByTag(SyncWorker.TAG)
  }
}
