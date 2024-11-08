package org.simple.clinic.purge

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Scheduler
import io.reactivex.Single
import org.simple.clinic.AppDatabase
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SyncProgress
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.Instant
import javax.inject.Inject

class PurgeWorker(
    context: Context,
    workerParams: WorkerParameters,
) : RxWorker(context, workerParams) {

  companion object {
    const val PURGE_WORKER = "purge_worker"
    private const val NOTIFICATION_CHANNEL_ID = "org.simple.clinic.purge"
    private const val NOTIFICATION_CHANNEL_NAME = "Database Maintainence"
    private const val NOTIFICATION_ID = 300

    fun createWorkRequest(): OneTimeWorkRequest {
      return OneTimeWorkRequestBuilder<PurgeWorker>()
          .build()
    }
  }

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var lastSyncedState: Preference<LastSyncedState>

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var userClock: UserClock

  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  init {
    ClinicApp.appComponent.inject(this)
  }

  override fun createWork(): Single<Result> {
    setForegroundAsync(createForegroundInfo())

    return Single.create {
      val lastSyncProgress = lastSyncedState.get().lastSyncProgress

      // If sync is already in progress, we want to avoid doing purge operation
      // to avoid causing data corruptions.
      if (lastSyncProgress != SyncProgress.SYNCING) {
        appDatabase.prune(Instant.now(userClock))
        it.onSuccess(Result.success())
      }

      it.onSuccess(Result.failure())
    }
  }

  private fun createForegroundInfo(): ForegroundInfo {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createChannel()
    }

    val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
        .setContentTitle(applicationContext.getString(R.string.purge_notification_title))
        .setContentText(applicationContext.getString(R.string.purge_notification_desc))
        .setSmallIcon(R.drawable.ic_database)
        .setProgress(0, 0, true)
        .setOngoing(true)
        .build()

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    } else {
      ForegroundInfo(NOTIFICATION_ID, notification)
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createChannel() {
    notificationManager.createNotificationChannel(NotificationChannel(
        NOTIFICATION_CHANNEL_ID,
        NOTIFICATION_CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH,
    ))
  }

  override fun getBackgroundScheduler(): Scheduler {
    return schedulersProvider.io()
  }
}
