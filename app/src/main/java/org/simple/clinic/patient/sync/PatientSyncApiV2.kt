package org.simple.clinic.patient.sync

import io.reactivex.Single
import org.simple.clinic.sync.DataPushResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PatientSyncApiV2 {

  companion object {
    const val version = "v2"
  }

  @POST("$version/patients/sync")
  fun push(
      @Body body: PatientPushRequest
  ): Single<DataPushResponse>

  @GET("$version/patients/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullTimestamp: String? = null
  ): Single<PatientPullResponse>
}
