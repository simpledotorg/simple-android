package org.simple.clinic.drugstockreminders

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DrugStockReminderApi {

  @GET("v4/drug_stock")
  fun drugStockReminder(
      @Query("date") previousMonth: String
  ): Call<DrugStockReminderResponsePayload>
}
