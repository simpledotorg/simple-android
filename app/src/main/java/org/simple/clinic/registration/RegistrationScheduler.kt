package org.simple.clinic.registration

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RegistrationScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val config: Single<RegistrationConfig>
) {

  fun schedule(): Completable {
    return config
        .map { config ->
          val constraints = Constraints.Builder()
              .setRequiredNetworkType(NetworkType.CONNECTED)
              .setRequiresBatteryNotLow(false)
              .build()

          OneTimeWorkRequestBuilder<RegistrationWorker>()
              .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, config.retryBackOffDelayInMinutes, TimeUnit.MINUTES)
              .setConstraints(constraints)
              .build()
        }
        .flatMapCompletable { request ->
          Completable.fromAction {
            workManager.enqueue(request)
          }
        }
  }
}
