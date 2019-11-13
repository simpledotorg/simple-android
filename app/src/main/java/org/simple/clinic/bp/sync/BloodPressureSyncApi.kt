package org.simple.clinic.bp.sync

import io.reactivex.Single
import org.simple.clinic.sync.DataPushResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface BloodPressureSyncApi {

  @POST("blood_pressures/sync")
  fun push(
      @Body body: BloodPressurePushRequest
  ): Single<DataPushResponse>

  @Headers(value = ["X-RESYNC-TOKEN: 1"])
  @GET("blood_pressures/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Single<BloodPressurePullResponse>
}
