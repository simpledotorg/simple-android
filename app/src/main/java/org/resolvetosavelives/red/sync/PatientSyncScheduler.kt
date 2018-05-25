package org.resolvetosavelives.red.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PatientSyncScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val syncConfigProvider: Single<PatientSyncConfig>
) {

  fun schedule(): Completable {
    return syncConfigProvider
        .map { config ->
          val constraints = Constraints.Builder()
              .setRequiredNetworkType(NetworkType.CONNECTED)
              .setRequiresBatteryNotLow(true)
              .build()

          PeriodicWorkRequestBuilder<PatientSyncWorker>(config.frequency.toMillis(), TimeUnit.MILLISECONDS)
              .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
              .setConstraints(constraints)
              .addTag(PatientSyncWorker.TAG)
              .build()
        }
        .flatMapCompletable { request ->
          Completable.fromAction({
            workManager.cancelAllWorkByTag(PatientSyncWorker.TAG)
            workManager.enqueue(request)
          })
        }
  }
}
