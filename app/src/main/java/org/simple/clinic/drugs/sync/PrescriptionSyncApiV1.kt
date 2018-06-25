package org.simple.clinic.drugs.sync

import io.reactivex.Single
import org.simple.clinic.patient.sync.DataPushResponse
import org.threeten.bp.Instant
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PrescriptionSyncApiV1 {

  companion object {
    const val version = "v1"
  }

  @POST("$version/prescription_drugs/sync")
  fun push(
      @Body body: PrescriptionPushRequest
  ): Single<DataPushResponse>

  @GET("$version/prescription_drugs/sync")
  fun pull(
      @Query("limit") recordsToPull: Int
  ): Single<PrescriptionPullResponse>

  @GET("$version/prescription_drugs/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("processed_since") lastPullTimestamp: Instant
  ): Single<PrescriptionPullResponse>
}
