package org.simple.clinic.patient.download

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import java.time.LocalDate
import javax.inject.Inject

class PatientLineListScheduler @Inject constructor(
    private val workManager: WorkManager
) {

  fun schedule(
      fileFormat: PatientLineListFileFormat,
      bpCreatedAfter: LocalDate,
      bpCreatedBefore: LocalDate
  ) {
    val workRequest = PatientLinetListDownloadWorker.workRequest(
        bpCreatedAfter = bpCreatedAfter,
        bpCreatedBefore = bpCreatedBefore,
        fileFormat = fileFormat
    )

    workManager.enqueueUniqueWork(
        PatientLinetListDownloadWorker.TAG,
        ExistingWorkPolicy.REPLACE,
        workRequest
    )
  }
}
