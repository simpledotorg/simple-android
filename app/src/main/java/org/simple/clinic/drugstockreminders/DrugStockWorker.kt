package org.simple.clinic.drugstockreminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Single
import org.simple.clinic.ClinicApp
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.Found
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.NotFound
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.OtherError
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.UpdateDrugStockReportsMonth
import org.simple.clinic.util.UserClock
import java.time.LocalDate
import java.util.Optional
import javax.inject.Inject

class DrugStockWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : RxWorker(context, workerParams) {

  companion object {
    private const val NOTIFICATION_CHANNEL_ID = "org.simple.clinic.drugstockreminders"
    private const val NOTIFICATION_CHANNEL_NAME = "Drug Stock Reminders"
  }

  @Inject
  lateinit var clock: UserClock

  @Inject
  lateinit var drugStockReminder: DrugStockReminder

  @Inject
  @TypedPreference(UpdateDrugStockReportsMonth)
  lateinit var updateDrugStockReportsMonth: Preference<Optional<String>>

  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  init {
    ClinicApp.appComponent.inject(this)
  }

  override fun createWork(): Single<Result> {
    createNotificationChannel()

    val formattedDate = LocalDate.now(clock).minusMonths(1).toString()

    return Single.create<Result?> {
      val response = drugStockReminder
          .reminderForDrugStock(formattedDate)
      when (response) {
        is Found -> { /* no op */ }
        NotFound -> drugStockReportNotFound(formattedDate)
        OtherError -> { /* no op */ }
      }
    }
  }

  private fun drugStockReportNotFound(previousMonthsDate: String): Result {
    updateDrugStockReportsMonth.set(Optional.of(previousMonthsDate))
    return Result.failure()
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
