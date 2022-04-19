package org.simple.clinic.drugstockreminders

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Single
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.di.DateFormatter
import org.simple.clinic.di.DateFormatter.Type.MonthAndYear
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.Found
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.NotFound
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.OtherError
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.UpdateDrugStockReportsMonth
import org.simple.clinic.setup.SetupActivity
import org.simple.clinic.util.UserClock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Optional
import javax.inject.Inject

class DrugStockWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : RxWorker(context, workerParams) {

  companion object {
    private const val NOTIFICATION_CHANNEL_ID = "org.simple.clinic.drugstockreminders"
    private const val NOTIFICATION_CHANNEL_NAME = "Drug Stock Reminders"
    private const val NOTIFICATION_ID = 6
  }

  @Inject
  lateinit var clock: UserClock

  @Inject
  lateinit var drugStockReminder: DrugStockReminder

  @Inject
  @TypedPreference(UpdateDrugStockReportsMonth)
  lateinit var updateDrugStockReportsMonth: Preference<Optional<String>>

  @Inject
  @DateFormatter(MonthAndYear)
  lateinit var monthAndYearDateFormatter: DateTimeFormatter

  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  init {
    ClinicApp.appComponent.inject(this)
  }

  override fun createWork(): Single<Result> {
    createNotificationChannel()

    val formattedDate = LocalDate.now(clock).minusMonths(1).toString()

    return Single.create {
      when (val response = drugStockReminder.reminderForDrugStock(formattedDate)) {
        is Found -> drugStockReportFound(response.drugStockReminderResponse.month)
        NotFound -> drugStockReportNotFound(formattedDate)
        OtherError -> {
          /* no op */
        }
      }
    }
  }

  private fun drugStockReportNotFound(previousMonthsDate: String): Result {
    updateDrugStockReportsMonth.set(Optional.of(previousMonthsDate))
    notificationManager.notify(NOTIFICATION_ID, drugStockReminderNotification(previousMonthsDate))
    return Result.failure()
  }

  private fun drugStockReportFound(currentMonthsDate: String): Result {
    updateDrugStockReportsMonth.set(Optional.of(currentMonthsDate))
    return Result.success()
  }

  private fun drugStockReminderNotification(currentMonthsDate: String): Notification {
    val monthAndYear = formatDateForNotification(currentMonthsDate)
    val notificationHeaderText = context.getString(R.string.drug_stock_reminder_notification, monthAndYear)

    return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_app_update_notification_logo)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(context.getString(R.string.drug_stock_reminder_notification, monthAndYear))
        .setTicker(context.getString(R.string.app_name))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setStyle(NotificationCompat.BigTextStyle().bigText(notificationHeaderText))
        .setContentIntent(openAppToHomeScreen())
        .build()
  }

  private fun formatDateForNotification(currentMonthsDate: String): String {
    val date = LocalDate.parse(currentMonthsDate)
    return monthAndYearDateFormatter.format(date)
  }

  private fun openAppToHomeScreen(): PendingIntent? {
    val intent = Intent(context, SetupActivity::class.java)
    val flag = setFlagBasedOnAndroidApis()
    return PendingIntent.getActivity(context, 0, intent, flag)
  }

  private fun setFlagBasedOnAndroidApis(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
      PendingIntent.FLAG_CANCEL_CURRENT
    }
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
