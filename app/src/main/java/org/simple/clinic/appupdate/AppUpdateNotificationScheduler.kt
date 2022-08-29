package org.simple.clinic.appupdate

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import org.simple.clinic.appupdate.AppUpdateNotificationWorker.Companion.APP_UPDATE_NOTIFICATION_WORKER
import org.simple.clinic.util.UserClock
import java.time.LocalDate
import javax.inject.Inject

class AppUpdateNotificationScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val userClock: UserClock
) {

  fun schedule() {
    val schedule = AppUpdateNotificationSchedule(LocalDate.now(userClock).atTime(8, 0))

    val workRequest = AppUpdateNotificationWorker.createWorkRequest(userClock, schedule)

    workManager.enqueueUniqueWork(
        APP_UPDATE_NOTIFICATION_WORKER,
        ExistingWorkPolicy.REPLACE,
        workRequest
    )
  }
}
