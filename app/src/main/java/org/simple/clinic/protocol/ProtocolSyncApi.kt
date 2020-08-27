package org.simple.clinic.protocol

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ProtocolSyncApi {

  @Headers(value = ["X-RESYNC-TOKEN: 1"])
  @GET("v3/protocols/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Call<ProtocolPullResponse>
}
