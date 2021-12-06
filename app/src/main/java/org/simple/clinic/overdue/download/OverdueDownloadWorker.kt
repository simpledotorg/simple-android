package org.simple.clinic.overdue.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.reactivex.Scheduler
import io.reactivex.Single
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.overdue.download.OverdueListFileFormat.CSV
import org.simple.clinic.overdue.download.OverdueListFileFormat.PDF
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class OverdueDownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : RxWorker(context, workerParams) {

  companion object {
    const val OVERDUE_DOWNLOAD_WORKER = "overdue_download_worker"

    private const val KEY_DOWNLOAD_FORMAT = "download_format"
    private const val NOTIFICATION_CHANNEL_ID = "org.simple.clinic.Downloads"
    private const val NOTIFICATION_CHANNEL_NAME = "Downloads"

    private const val DOWNLOAD_IN_PROGRESS_NOTIFICATION_ID = 1
    private const val DOWNLOAD_SUCCESS_NOTIFICATION_ID = 2
    private const val DOWNLOAD_FAILED_NOTIFICATION_ID = 3

    private const val PLAY_STORE_SHEETS = "https://play.google.com/store/apps/details?id=com.google.android.apps.docs.editors.sheets"
    private const val PLAY_STORE_ADOBE_ACROBAT = "https://play.google.com/store/apps/details?id=com.adobe.reader"

    private const val GOOGLE_SHEETS_PACKAGE_NAME = "com.google.android.apps.docs.editors.sheets"

    fun workRequest(fileFormat: OverdueListFileFormat): OneTimeWorkRequest {
      return OneTimeWorkRequestBuilder<OverdueDownloadWorker>()
          .setInputData(workDataOf(
              KEY_DOWNLOAD_FORMAT to fileFormat.toString()
          ))
          .build()
    }
  }

  @Inject
  lateinit var downloader: OverdueListDownloader

  @Inject
  lateinit var workManager: WorkManager

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  init {
    ClinicApp.appComponent.inject(this)
  }

  override fun getBackgroundScheduler(): Scheduler {
    return schedulersProvider.io()
  }

  override fun createWork(): Single<Result> {
    createNotificationChannel()
    setForegroundAsync(downloadInProgressNotification()).get()

    val downloadFormatString = inputData.getString(KEY_DOWNLOAD_FORMAT)!!
    val downloadFormat = OverdueListFileFormat.valueOf(downloadFormatString)

    return downloader
        .download(downloadFormat)
        .map { result ->
          when (result) {
            is OverdueListDownloadResult.DownloadSuccessful -> downloadSuccess(result.uri, downloadFormat)
            OverdueListDownloadResult.DownloadFailed,
            OverdueListDownloadResult.NotEnoughStorage -> downloadFailure()
          }
        }
  }

  override fun onStopped() {
    notificationManager.cancel(DOWNLOAD_IN_PROGRESS_NOTIFICATION_ID)
    super.onStopped()
  }

  private fun downloadFailure(): Result {
    notificationManager.run {
      cancel(DOWNLOAD_IN_PROGRESS_NOTIFICATION_ID)
      notify(DOWNLOAD_FAILED_NOTIFICATION_ID, downloadFailedNotification())
    }

    return Result.failure()
  }

  private fun downloadSuccess(uri: Uri, fileFormat: OverdueListFileFormat): Result {
    notificationManager.run {
      cancel(DOWNLOAD_IN_PROGRESS_NOTIFICATION_ID)
      notify(DOWNLOAD_SUCCESS_NOTIFICATION_ID, downloadSucceededNotification(uri, fileFormat))
    }

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

  private fun downloadInProgressNotification(): ForegroundInfo {
    val cancelPendingIntent = workManager.createCancelPendingIntent(id)

    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_download_notification_icon)
        .setContentTitle(context.getString(R.string.overdue_download_notification_in_progress_title))
        .setTicker(context.getString(R.string.overdue_download_notification_in_progress_title))
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setProgress(0, 0, true)
        .addAction(android.R.drawable.ic_delete, context.getString(R.string.overdue_download_notification_cancel), cancelPendingIntent)
        .build()

    return ForegroundInfo(DOWNLOAD_IN_PROGRESS_NOTIFICATION_ID, notification)
  }

  private fun downloadFailedNotification(): Notification {
    return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_download_notification_icon)
        .setContentTitle(context.getString(R.string.overdue_download_notification_failed_title))
        .setTicker(context.getString(R.string.overdue_download_notification_failed_title))
        .setContentText(context.getString(R.string.overdue_download_notification_failed_subtitle))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()
  }

  private fun downloadSucceededNotification(uri: Uri, fileFormat: OverdueListFileFormat): Notification {
    val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
    } else {
      PendingIntent.FLAG_CANCEL_CURRENT
    }

    var intent = Intent(Intent.ACTION_VIEW).apply {
      setDataAndType(uri, fileFormat.mimeType)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    val playStoreUrl = when (fileFormat) {
      CSV -> PLAY_STORE_SHEETS
      PDF -> PLAY_STORE_ADOBE_ACROBAT
    }

    if (fileFormat == CSV) intent.setPackage(GOOGLE_SHEETS_PACKAGE_NAME)

    if (intent.resolveActivity(context.packageManager) == null) {
      intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
    }

    val pendingIntent = PendingIntent.getActivity(context, 0, intent, flag)

    return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_download_notification_icon)
        .setContentTitle(context.getString(R.string.overdue_download_notification_succeeded_title))
        .setTicker(context.getString(R.string.overdue_download_notification_succeeded_title))
        .setContentText(context.getString(R.string.overdue_download_notification_succeeded_subtitle))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()
  }
}
