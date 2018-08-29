package org.simple.clinic.overdue

import io.reactivex.Single
import org.simple.clinic.sync.DataPushResponse
import org.threeten.bp.Instant
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AppointmentSyncApiV1 {

  @POST("v1/appointments/sync")
  fun push(
      @Body body: AppointmentPushRequest
  ): Single<DataPushResponse>

  @GET("v1/appointments/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("processed_since") lastPullTimestamp: Instant? = null
  ): Single<AppointmentPullResponse>
}
