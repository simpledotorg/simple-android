package org.simple.clinic.overdue.download

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import org.simple.clinic.overdue.download.OverdueDownloadWorker.Companion.OVERDUE_DOWNLOAD_WORKER
import java.util.UUID
import javax.inject.Inject

class OverdueDownloadScheduler @Inject constructor(
    private val workManager: WorkManager
) {

  fun schedule(fileFormat: OverdueListFileFormat, selectedAppointmentIds: Set<UUID> = emptySet()) {
    val workRequest = OverdueDownloadWorker.workRequest(fileFormat, selectedAppointmentIds)

    workManager.enqueueUniqueWork(
        OVERDUE_DOWNLOAD_WORKER,
        ExistingWorkPolicy.REPLACE,
        workRequest
    )
  }
}
