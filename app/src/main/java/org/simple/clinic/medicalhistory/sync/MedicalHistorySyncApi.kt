package org.simple.clinic.medicalhistory.sync

import io.reactivex.Single
import org.simple.clinic.CURRENT_API_VERSION
import org.simple.clinic.sync.DataPushResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MedicalHistorySyncApi {

  @POST("$CURRENT_API_VERSION/medical_histories/sync")
  fun push(
      @Body body: MedicalHistoryPushRequest
  ): Single<DataPushResponse>

  @GET("$CURRENT_API_VERSION/medical_histories/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Single<MedicalHistoryPullResponse>
}
