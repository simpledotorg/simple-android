package org.simple.clinic.patient.download

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import javax.inject.Inject

class PatientLineListScheduler @Inject constructor(
    private val workManager: WorkManager
) {

  fun schedule(fileFormat: PatientLineListFileFormat) {
    val workRequest = PatientLinetListDownloadWorker.workRequest(
        fileFormat = fileFormat
    )

    workManager.enqueueUniqueWork(
        PatientLinetListDownloadWorker.TAG,
        ExistingWorkPolicy.REPLACE,
        workRequest
    )
  }
}
