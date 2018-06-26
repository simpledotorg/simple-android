package org.simple.clinic.facility

import io.reactivex.Single
import org.threeten.bp.Instant
import retrofit2.http.GET
import retrofit2.http.Query

interface FacilitySyncApiV1 {

  companion object {
    const val version = "v1"
  }

  @GET("$version/facilities/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("processed_since") lastPullTimestamp: Instant? = null
  ): Single<FacilityPullResponse>

}
