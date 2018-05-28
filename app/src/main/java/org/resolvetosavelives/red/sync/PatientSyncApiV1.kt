package org.resolvetosavelives.red.sync

import io.reactivex.Single
import org.threeten.bp.Instant
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PatientSyncApiV1 {

  companion object {
    const val version = "v1"
  }

  @POST("$version/patients/sync")
  fun push(
      @Body body: PatientPushRequest
  ): Single<PatientPushResponse>

  @GET("$version/patients/sync")
  fun pull(
      @Query("processed_since") latestRecordTimestamp: Instant? = null,
      @Query("first_time") isFirstSync: Boolean,
      @Query("limit") recordsToRetrieve: Int
  ): Single<PatientPullResponse>
}
