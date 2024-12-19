package org.simple.clinic.cvdrisk.sync

import org.simple.clinic.sync.DataPushResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CVDRiskSyncApi {

  @POST("v4/cvd_risks/sync")
  fun push(
      @Body body: CVDRiskPushRequest
  ): Call<DataPushResponse>

  @GET("v4/cvd_risks/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Call<CVDRiskPullResponse>
}
