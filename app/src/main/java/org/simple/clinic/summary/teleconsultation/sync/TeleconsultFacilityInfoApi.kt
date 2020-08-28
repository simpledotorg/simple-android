package org.simple.clinic.summary.teleconsultation.sync

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface TeleconsultFacilityInfoApi {
  @Headers(value = ["X-RESYNC-TOKEN: 1"])
  @GET("v4/teleconsultation_medical_officers/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Call<TeleconsultFacilityInfoPullResponse>
}
