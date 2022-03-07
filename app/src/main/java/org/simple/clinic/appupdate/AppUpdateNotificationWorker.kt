package org.simple.clinic.appupdate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Scheduler
import io.reactivex.Single
import org.simple.clinic.R
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL_SECURITY
import org.simple.clinic.appupdate.AppUpdateNudgePriority.LIGHT
import org.simple.clinic.appupdate.AppUpdateNudgePriority.MEDIUM
import org.simple.clinic.appupdate.AppUpdateState.DontShowAppUpdate
import org.simple.clinic.appupdate.AppUpdateState.ShowAppUpdate
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.IsLightAppUpdateNotificationShown
import org.simple.clinic.main.TypedPreference.Type.IsMediumAppUpdateNotificationShown
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
    const val PLAY_STORE_URL_FOR_SIMPLE = "https://play.google.com/store/apps/details?id=org.simple.clinic"

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

  @Inject
  @TypedPreference(IsLightAppUpdateNotificationShown)
  lateinit var isLightAppUpdateNotificationShown: Preference<Boolean>

  @Inject
  @TypedPreference(IsMediumAppUpdateNotificationShown)
  lateinit var isMediumAppUpdateNotificationShown: Preference<Boolean>

  override fun createWork(): Single<Result> {
    createNotificationChannel()

    return checkAppUpdateAvailability
        .listen()
        .filter { it is ShowAppUpdate || it is DontShowAppUpdate }
        .singleOrError()
        .map { result ->
          if (result is ShowAppUpdate) {
            showAppUpdateNotificationBasedOnThePriority(result)
          } else {
            resetPreferences()
          }

          Result.success()
        }
        .doOnError {
          Result.failure()
        }
  }

  private fun resetPreferences() {
    isLightAppUpdateNotificationShown.set(false)
    isMediumAppUpdateNotificationShown.set(false)
  }

  private fun showAppUpdateNotificationBasedOnThePriority(showAppUpdate: ShowAppUpdate) {
    if (showAppUpdate.appUpdateNudgePriority == null)
      return

    when (showAppUpdate.appUpdateNudgePriority) {
      LIGHT -> showAppUpdateNotification(NOTIFICATION_ID_LIGHT, isLightAppUpdateNotificationShown)
      MEDIUM -> showAppUpdateNotification(NOTIFICATION_ID_MEDIUM, isMediumAppUpdateNotificationShown)
      CRITICAL_SECURITY, CRITICAL -> showCriticalAppUpdateNotification()
    }
  }

  override fun getBackgroundScheduler(): Scheduler {
    return schedulerProvider.io()
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

  private fun showAppUpdateNotification(notificationId: Int, isAppUpdateNotificationShown: Preference<Boolean>) {
    if (!isAppUpdateNotificationShown.get()) {

      val contentText = context.getString(R.string.app_update_notification_subtext)
      val appUpdateNotification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
          .setSmallIcon(R.drawable.ic_app_update_notification_logo)
          .setContentTitle(context.getString(R.string.app_name))
          .setTicker(context.getString(R.string.app_name))
          .setContentText(context.getString(R.string.app_update_notification_subtext))
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
          .setContentIntent(openSimpleInPlayStorePendingIntent())
          .build()

      isAppUpdateNotificationShown.set(true)

      notificationManager.notify(notificationId, appUpdateNotification)
    }
  }

  private fun openSimpleInPlayStorePendingIntent(): PendingIntent {
    val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
    } else {
      PendingIntent.FLAG_CANCEL_CURRENT
    }

    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL_FOR_SIMPLE))

    return PendingIntent.getActivity(context, 0, intent, flag)
  }

  private fun showCriticalAppUpdateNotification() {
    val contentText = context.getString(R.string.critical_app_update_notification_subtext)
    val criticalAppUpdateNotification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_app_update_notification_logo)
        .setContentTitle(context.getString(R.string.app_name))
        .setTicker(context.getString(R.string.app_name))
        .setContentText(contentText)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
        .setContentIntent(openSimpleInPlayStorePendingIntent())
        .build()

    notificationManager.notify(NOTIFICATION_CRITICAL, criticalAppUpdateNotification)
  }
}
