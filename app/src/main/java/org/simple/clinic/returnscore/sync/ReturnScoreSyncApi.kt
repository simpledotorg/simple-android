package org.simple.clinic.returnscore.sync

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ReturnScoreSyncApi {

  @Headers(value = ["X-RESYNC-TOKEN: 1"])
  @GET("v4/patient_scores/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null,
  ): Call<ReturnScorePullResponse>
}

