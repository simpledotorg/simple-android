package org.simple.clinic.patientattribute.sync

import org.simple.clinic.sync.DataPushResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PatientAttributeSyncApi {

  @POST("v4/patient_attributes/sync")
  fun push(
      @Body body: PatientAttributePushRequest
  ): Call<DataPushResponse>

  @GET("v4/patient_attributes/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Call<PatientAttributePullResponse>
}
