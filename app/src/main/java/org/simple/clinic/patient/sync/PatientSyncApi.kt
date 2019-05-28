package org.simple.clinic.patient.sync

import io.reactivex.Single
import org.simple.clinic.CURRENT_API_VERSION
import org.simple.clinic.sync.DataPushResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PatientSyncApi {

  @POST("$CURRENT_API_VERSION/patients/sync")
  fun push(
      @Body body: PatientPushRequest
  ): Single<DataPushResponse>

  @GET("$CURRENT_API_VERSION/patients/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullTimestamp: String? = null
  ): Single<PatientPullResponse>
}
