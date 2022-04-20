package org.simple.clinic.drugstockreminders

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DrugStockReminderApi {

  @GET("v4/drug_stocks")
  fun drugStockReminder(
      @Query("date") previousMonth: String
  ): Call<DrugStockReminderResponsePayload>
}
