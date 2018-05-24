package org.resolvetosavelives.red.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.WorkManager
import androidx.work.ktx.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PatientSyncScheduler @Inject constructor(private val workManager: WorkManager) {

  fun schedule(interval: Long, intervalUnit: TimeUnit) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    val request = PeriodicWorkRequestBuilder<PatientSyncWorker>(interval, intervalUnit)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .addTag(PatientSyncWorker.TAG)
        .build()

    workManager.cancelAllWorkByTag(PatientSyncWorker.TAG)
    workManager.enqueue(request)
  }
}
