package org.simple.clinic.drugs.sync

import org.simple.clinic.sync.DataPushResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface PrescriptionSyncApi {

  @POST("v3/prescription_drugs/sync")
  fun push(
      @Body body: PrescriptionPushRequest
  ): Call<DataPushResponse>

  @Headers(value = ["X-RESYNC-TOKEN: 1"])
  @GET("v3/prescription_drugs/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Call<PrescriptionPullResponse>
}
