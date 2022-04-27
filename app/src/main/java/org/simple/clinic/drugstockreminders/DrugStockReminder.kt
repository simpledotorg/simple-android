package org.simple.clinic.drugstockreminders

import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.Found
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.NotFound
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.OtherError
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.DrugStockFormURL
import org.simple.clinic.platform.crash.CrashReporter
import retrofit2.Response
import java.util.Optional
import javax.inject.Inject

class DrugStockReminder @Inject constructor(
    private val drugStockReminderApi: DrugStockReminderApi,
    @TypedPreference(DrugStockFormURL) private val drugStockFormUrl: Preference<Optional<String>>
) {

  fun reminderForDrugStock(date: String): Result {
    return try {
      drugStockReminder(date)
    } catch (e: Exception) {
      CrashReporter.report(e)
      OtherError
    }
  }

  private fun drugStockReminder(date: String): Result {
    val response = drugStockReminderApi
        .drugStockReminder(
            previousMonth = date,
        ).execute()

    return when (response.code()) {
      200 -> readResponse(response)
      404 -> NotFound
      else -> OtherError
    }
  }

  private fun readResponse(
      response: Response<DrugStockReminderResponsePayload>
  ): Result {
    val responseBody = response.body()!!
    val drugStockReports = responseBody
        .drugs
        .map(::convertResponseToDrugStockReports)

    drugStockFormUrl.set(Optional.of(responseBody.drugStockFormUrl))

    return if (drugStockReports.isNotEmpty())
      Found(responseBody)
    else
      NotFound
  }

  private fun convertResponseToDrugStockReports(
      drugsStockReportsResponse: DrugStockReportPayload
  ): DrugStockReport {
    return DrugStockReport(
        protocolDrugId = drugsStockReportsResponse.protocolDrugId,
        drugsInStock = drugsStockReportsResponse.drugsInStock,
        drugsReceived = drugsStockReportsResponse.drugsReceived
    )
  }

  sealed class Result {
    data class Found(val drugStockReminderResponse: DrugStockReminderResponsePayload) : Result()
    object NotFound : Result()
    object OtherError : Result()
  }
}
