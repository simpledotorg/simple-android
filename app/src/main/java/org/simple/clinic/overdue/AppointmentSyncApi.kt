package org.simple.clinic.overdue

import org.simple.clinic.sync.DataPushResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface AppointmentSyncApi {

  @POST("v3/appointments/sync")
  fun push(
      @Body body: AppointmentPushRequest
  ): Call<DataPushResponse>

  @Headers(value = ["X-RESYNC-TOKEN: 3"])
  @GET("v3/appointments/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Call<AppointmentPullResponse>
}
