package org.simple.clinic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.simple.clinic.sync.DataSync
import javax.inject.Inject

object DebugNotification {

  private const val NOTIF_CHANNEL_ID = "debug"
  private const val NOTIF_ID = 0

  fun show(context: Context, appSignature: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      notificationManager.createNotificationChannel(NotificationChannel(NOTIF_CHANNEL_ID, "Debug", NotificationManager.IMPORTANCE_MIN))
    }

    val syncPendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, DebugNotificationActionReceiver::class.java),
        PendingIntent.FLAG_CANCEL_CURRENT)
    val syncAction = NotificationCompat.Action(R.drawable.ic_favorite_20dp, "Sync data", syncPendingIntent)

    val notif = NotificationCompat.Builder(context, NOTIF_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("${context.getString(R.string.app_name)}: $appSignature")
        .setContentText("This notification will only be visible on debug builds.")
        .setPriority(NotificationCompat.PRIORITY_MIN)
        .addAction(syncAction)
        .build()
    notificationManager.notify(NOTIF_ID, notif)
  }

  fun stop(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(NOTIF_ID)
  }
}

class DebugNotificationActionReceiver : BroadcastReceiver() {

  @Inject
  lateinit var dataSync: DataSync

  override fun onReceive(context: Context?, intent: Intent?) {
    DebugClinicApp.appComponent().inject(this)

    dataSync.fireAndForgetSync()
  }
}
