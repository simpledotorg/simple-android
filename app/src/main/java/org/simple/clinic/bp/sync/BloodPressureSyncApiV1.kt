package org.simple.clinic.bp.sync

import io.reactivex.Single
import org.simple.clinic.sync.DataPushResponse
import org.threeten.bp.Instant
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BloodPressureSyncApiV1 {

  companion object {
    const val version = "v1"
  }

  @POST("$version/blood_pressures/sync")
  fun push(
      @Body body: BloodPressurePushRequest
  ): Single<DataPushResponse>

  @GET("$version/blood_pressures/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("processed_since") lastPullTimestamp: Instant? = null
  ): Single<BloodPressurePullResponse>
}
