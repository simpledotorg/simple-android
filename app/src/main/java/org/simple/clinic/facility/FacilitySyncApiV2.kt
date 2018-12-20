package org.simple.clinic.facility

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface FacilitySyncApiV2 {

  companion object {
    const val version = "v2"
  }

  @GET("$version/facilities/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Single<FacilityPullResponse>

}
