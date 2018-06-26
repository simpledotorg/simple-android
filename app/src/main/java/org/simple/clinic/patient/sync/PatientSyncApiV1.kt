package org.simple.clinic.patient.sync

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
  ): Single<DataPushResponse>

  // TODO: Remove this function, and reuse other pull function with nullable lastPullTimestamp
  @GET("$version/patients/sync")
  fun pull(
      @Query("limit") recordsToPull: Int
  ): Single<PatientPullResponse>

  @GET("$version/patients/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("processed_since") lastPullTimestamp: Instant
  ): Single<PatientPullResponse>
}
