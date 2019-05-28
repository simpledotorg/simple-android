package org.simple.clinic.bp.sync

import io.reactivex.Single
import org.simple.clinic.CURRENT_API_VERSION
import org.simple.clinic.sync.DataPushResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BloodPressureSyncApi {

  @POST("$CURRENT_API_VERSION/blood_pressures/sync")
  fun push(
      @Body body: BloodPressurePushRequest
  ): Single<DataPushResponse>

  @GET("$CURRENT_API_VERSION/blood_pressures/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Single<BloodPressurePullResponse>
}
