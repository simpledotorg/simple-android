package org.simple.clinic.drugstockreminders

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET

interface DrugStockReminderApi {

  @GET("v4/drug_stock")
  fun drugStockReminder(
      @Body body: DrugStockReminderRequest
  ): Call<DrugStockReminderResponsePayload>
}
