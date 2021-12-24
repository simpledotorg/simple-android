package org.simple.clinic.patient.sync

import org.simple.clinic.di.network.Timeout
import org.simple.clinic.patient.onlinelookup.api.OnlineLookupResponsePayload
import org.simple.clinic.patient.onlinelookup.api.PatientOnlineLookupRequest
import org.simple.clinic.sync.DataPushResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface PatientSyncApi {

  @POST("v3/patients/sync")
  fun push(
      @Body body: PatientPushRequest
  ): Call<DataPushResponse>

  @Headers(value = ["X-RESYNC-TOKEN: 3"])
  @GET("v3/patients/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullTimestamp: String? = null
  ): Call<PatientPullResponse>

  @Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
  @POST("v4/patients/lookup")
  fun lookup(
      @Body body: PatientOnlineLookupRequest
  ): Call<OnlineLookupResponsePayload>
}
