package org.simple.clinic.facility

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface FacilitySyncApi {

  @Headers(value = ["X-RESYNC-TOKEN: 3"])
  @GET("v3/facilities/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Call<FacilityPullResponse>

}
