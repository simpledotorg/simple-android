package org.simple.clinic.facility

import io.reactivex.Single
import org.simple.clinic.CURRENT_API_VERSION
import retrofit2.http.GET
import retrofit2.http.Query

interface FacilitySyncApi {

  @GET("$CURRENT_API_VERSION/facilities/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Single<FacilityPullResponse>

}
