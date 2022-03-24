package org.simple.clinic.drugstockreminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.reactivex.Single

class DrugStockWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : RxWorker(context, workerParams) {

  companion object {
    private const val NOTIFICATION_CHANNEL_ID = "org.simple.clinic.drugstockreminders"
    private const val NOTIFICATION_CHANNEL_NAME = "Drug Stock Reminders"
  }

  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  init {
    ClinicApp.appComponent.inject(this)
  }
  override fun createWork(): Single<Result> {
    createNotificationChannel()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      notificationManager.createNotificationChannel(
          NotificationChannel(
              NOTIFICATION_CHANNEL_ID,
              NOTIFICATION_CHANNEL_NAME,
              NotificationManager.IMPORTANCE_HIGH
          ))
    }
  }
}
