package org.simple.clinic.purge

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import org.simple.clinic.purge.PurgeWorker.Companion.PURGE_WORKER
import javax.inject.Inject

class PurgeScheduler @Inject constructor(
    private val workerManager: WorkManager,
) {

  fun run() {
    val workRequest = PurgeWorker.createWorkRequest()

    workerManager.enqueueUniqueWork(
        PURGE_WORKER,
        ExistingWorkPolicy.REPLACE,
        workRequest,
    )
  }
}
