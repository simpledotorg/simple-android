package org.simple.clinic.medicalhistory.sync

import io.reactivex.Single
import org.simple.clinic.sync.DataPushResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface MedicalHistorySyncApiV1 {

  @POST("v2/medical_histories/sync")
  fun push(
      @Body body: MedicalHistoryPushRequest
  ): Single<DataPushResponse>

  @GET("v2/medical_histories/sync")
  @Headers("HTTP_X_FACILITY_ID: 1bb26c0b-e0cb-4d5e-8582-47095a3e18bc")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("processed_since") lastPullToken: String? = null
  ): Single<MedicalHistoryPullResponse>

}
