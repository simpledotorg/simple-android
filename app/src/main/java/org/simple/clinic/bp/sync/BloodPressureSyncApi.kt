package org.simple.clinic.bp.sync

import org.simple.clinic.sync.DataPushResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface BloodPressureSyncApi {

  @POST("v3/blood_pressures/sync")
  fun push(
      @Body body: BloodPressurePushRequest
  ): Call<DataPushResponse>

  @Headers(value = ["X-RESYNC-TOKEN: 1"])
  @GET("v3/blood_pressures/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Call<BloodPressurePullResponse>
}
