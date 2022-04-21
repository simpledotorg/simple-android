package org.simple.clinic.drugstockreminders

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import org.simple.clinic.drugstockreminders.DrugStockWorker.Companion.DRUG_STOCK_NOTIFICATION_WORKER
import org.simple.clinic.util.UserClock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class DrugStockNotificationScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val userClock: UserClock
) {

  fun schedule() {
    val currentDateTime = LocalDateTime.now(userClock)
    val scheduledDateTime = scheduledDateTime(currentDateTime)

    val initialDelay = Duration.between(currentDateTime, scheduledDateTime)

    val workRequest = DrugStockWorker.createWorkRequest(
        initialDelay = initialDelay.toMillis()
    )

    workManager.enqueueUniqueWork(
        DRUG_STOCK_NOTIFICATION_WORKER,
        ExistingWorkPolicy.REPLACE,
        workRequest
    )
  }

  private fun scheduledDateTime(currentDateTime: LocalDateTime): LocalDateTime {
    val scheduledDateTime = LocalDate.now(userClock)
        .withDayOfMonth(1)
        .atTime(8, 0)

    return if (currentDateTime.isAfter(scheduledDateTime)) {
      scheduledDateTime
          .plusMonths(1)
          .withDayOfMonth(1)
    } else {
      scheduledDateTime
    }
  }
}
