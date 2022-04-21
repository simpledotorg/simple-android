package org.simple.clinic.drugstockreminders

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Scheduler
import io.reactivex.Single
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.di.DateFormatter
import org.simple.clinic.di.DateFormatter.Type.MonthAndYear
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.Found
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.NotFound
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.OtherError
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.DrugStockReportLastCheckedAt
import org.simple.clinic.main.TypedPreference.Type.IsDrugStockReportFilled
import org.simple.clinic.setup.SetupActivity
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Optional
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DrugStockWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : RxWorker(context, workerParams) {

  companion object {
    const val DRUG_STOCK_NOTIFICATION_WORKER = "drug_stock_notification_worker"

    private const val NOTIFICATION_CHANNEL_ID = "org.simple.clinic.drugstockreminders"
    private const val NOTIFICATION_CHANNEL_NAME = "Drug Stock Reminders"
    private const val NOTIFICATION_ID = 6

    fun createWorkRequest(
        initialDelay: Long
    ): OneTimeWorkRequest {
      return OneTimeWorkRequestBuilder<DrugStockWorker>()
          .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
          .build()
    }
  }

  @Inject
  lateinit var clock: UserClock

  @Inject
  lateinit var drugStockReminder: DrugStockReminder

  @Inject
  @TypedPreference(IsDrugStockReportFilled)
  lateinit var isDrugStockReportFilled: Preference<Optional<Boolean>>

  @Inject
  @TypedPreference(DrugStockReportLastCheckedAt)
  lateinit var drugStockReportLastCheckedAt: Preference<Instant>

  @Inject
  @DateFormatter(MonthAndYear)
  lateinit var monthAndYearDateFormatter: DateTimeFormatter

  @Inject
  lateinit var drugStockNotificationScheduler: DrugStockNotificationScheduler

  @Inject
  lateinit var schedulers: SchedulersProvider

  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  init {
    ClinicApp.appComponent.inject(this)
  }

  override fun createWork(): Single<Result> {
    createNotificationChannel()

    val previousMonthsDate = LocalDate.now(clock).minusMonths(1).toString()

    return Single.create<Result> {
      when (drugStockReminder.reminderForDrugStock(previousMonthsDate)) {
        is Found -> drugStockReportFound()
        NotFound -> drugStockReportNotFound(previousMonthsDate)
        OtherError -> {
          /* no op */
        }
      }
    }.doFinally {
      drugStockNotificationScheduler.schedule()
    }
  }

  override fun getBackgroundScheduler(): Scheduler {
    return schedulers.io()
  }

  private fun drugStockReportNotFound(previousMonthsDate: String): Result {
    notificationManager.notify(NOTIFICATION_ID, drugStockReminderNotification(previousMonthsDate))
    isDrugStockReportFilled.set(Optional.of(false))
    updateDrugStockLastCheckedAtPreference()
    return Result.failure()
  }

  private fun drugStockReportFound(): Result {
    isDrugStockReportFilled.set(Optional.of(true))
    updateDrugStockLastCheckedAtPreference()
    return Result.success()
  }

  private fun updateDrugStockLastCheckedAtPreference() {
    drugStockReportLastCheckedAt.set(Instant.now(clock))
  }

  private fun drugStockReminderNotification(currentMonthsDate: String): Notification {
    val monthAndYear = formatDateForNotification(currentMonthsDate)
    val notificationHeaderText = context.getString(R.string.drug_stock_reminder_notification, monthAndYear)

    return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_app_update_notification_logo)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(notificationHeaderText)
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
    val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
      PendingIntent.FLAG_CANCEL_CURRENT
    }

    return PendingIntent.getActivity(context, 0, intent, flag)
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
