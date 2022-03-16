package org.simple.clinic.appupdate

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import org.simple.clinic.appupdate.AppUpdateNotificationWorker.Companion.APP_UPDATE_NOTIFICATION_WORKER
import org.simple.clinic.util.UserClock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

class AppUpdateNotificationScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val userClock: UserClock
) {

  fun schedule() {
    val schedule = AppUpdateNotificationSchedule(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0, 0)))

    val workRequest = AppUpdateNotificationWorker.createWorkRequest(userClock, schedule)

    workManager.enqueueUniquePeriodicWork(
        APP_UPDATE_NOTIFICATION_WORKER,
        ExistingPeriodicWorkPolicy.REPLACE,
        workRequest
    )
  }
}
