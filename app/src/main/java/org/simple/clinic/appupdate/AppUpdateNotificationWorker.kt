package org.simple.clinic.appupdate

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.reactivex.Scheduler
import io.reactivex.Single
import org.simple.clinic.R
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL_SECURITY
import org.simple.clinic.appupdate.AppUpdateNudgePriority.LIGHT
import org.simple.clinic.appupdate.AppUpdateNudgePriority.MEDIUM
import org.simple.clinic.appupdate.AppUpdateState.ShowAppUpdate
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppUpdateNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : RxWorker(context, workerParams) {

  companion object {
    const val APP_UPDATE_NOTIFICATION_WORKER = "app_update_notification_worker"

    private const val NOTIFICATION_CHANNEL_ID = "org.simple.clinic.AppUpdates"
    private const val NOTIFICATION_ID_LIGHT = 4
    private const val NOTIFICATION_ID_MEDIUM = 5
    private const val NOTIFICATION_CRITICAL = 6
    private const val NOTIFICATION_CHANNEL_NAME = "Updates"

    fun createWorkRequest(userClock: UserClock, schedule: AppUpdateNotificationSchedule): OneTimeWorkRequest {
      val currentDateTime = LocalDateTime.now(userClock)
      val notificationScheduledTime = notificationScheduledTime(schedule.dateTime, currentDateTime)

      val initialDelay = Duration.between(currentDateTime, notificationScheduledTime).toMillis()

      return OneTimeWorkRequestBuilder<AppUpdateNotificationWorker>()
          .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
          .build()
    }

     private fun notificationScheduledTime(scheduledDateTime: LocalDateTime, currentDateTime: LocalDateTime): LocalDateTime {
      return if (scheduledDateTime.isAfter(currentDateTime)) {
        scheduledDateTime.plusDays(1)
      } else {
        scheduledDateTime
      }
    }
  }

  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  @Inject
  lateinit var checkAppUpdateAvailability: CheckAppUpdateAvailability

  @Inject
  lateinit var schedulerProvider: SchedulersProvider

  override fun createWork(): Single<Result> {
    createNotificationChannel()

    return checkAppUpdateAvailability
        .listen()
        .filter { it is ShowAppUpdate }
        .singleOrError()
        .map { result ->
          when ((result as ShowAppUpdate).appUpdateNudgePriority) {
            LIGHT -> showLightUpdateNotification()
            MEDIUM -> showMediumUpdateNotification()
            CRITICAL_SECURITY, CRITICAL -> showCriticalUpdateNotification()
            else -> Result.failure()
          }
        }
        .doOnError {
          Result.failure()
        }
  }

  override fun getBackgroundScheduler(): Scheduler {
    return schedulerProvider.io()
  }

  private fun showLightUpdateNotification(): Result {
    notificationManager.notify(NOTIFICATION_ID_LIGHT, appUpdateNotification())

    return Result.success()
  }

  private fun showMediumUpdateNotification(): Result {
    notificationManager.notify(NOTIFICATION_ID_MEDIUM, appUpdateNotification())

    return Result.success()
  }

  private fun showCriticalUpdateNotification(): Result {
    notificationManager.notify(NOTIFICATION_CRITICAL, criticalAppUpdateNotification())

    return Result.success()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      notificationManager.createNotificationChannel(NotificationChannel(
          NOTIFICATION_CHANNEL_ID,
          NOTIFICATION_CHANNEL_NAME,
          NotificationManager.IMPORTANCE_HIGH
      ))
    }
  }

  private fun appUpdateNotification(): Notification {
    val contentText = context.getString(R.string.app_update_notification_subtext)

    return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_app_update_notification_logo)
        .setContentTitle(context.getString(R.string.app_name))
        .setTicker(context.getString(R.string.app_name))
        .setContentText(contentText)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
        .build()
  }

  private fun criticalAppUpdateNotification(): Notification {
    val contentText = context.getString(R.string.critical_app_update_notification_subtext)
    
    return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_app_update_notification_logo)
        .setContentTitle(context.getString(R.string.app_name))
        .setTicker(context.getString(R.string.app_name))
        .setContentText(contentText)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
        .build()
  }
}
