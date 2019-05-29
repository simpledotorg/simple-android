package org.simple.clinic.facility

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface FacilitySyncApi {

  @Headers(value = ["X-RESYNC-TOKEN: 1"])
  @GET("facilities/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Single<FacilityPullResponse>

}
