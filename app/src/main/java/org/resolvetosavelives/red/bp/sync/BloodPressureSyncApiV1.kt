package org.resolvetosavelives.red.bp.sync

import io.reactivex.Single
import org.resolvetosavelives.red.patient.sync.DataPushResponse
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
      @Query("first_time") isFirstPull: Boolean
  ): Single<BloodPressurePullResponse>

  @GET("$version/blood_pressures/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("processed_since") lastPullTimestamp: Instant
  ): Single<BloodPressurePullResponse>
}
