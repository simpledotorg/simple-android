package org.simple.clinic.questionnaire.sync

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface QuestionnaireSyncApi {

  @Headers(value = ["X-RESYNC-TOKEN: 1"])
  @GET("v4/questionnaires/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null,
      @Query("dsl_version") dslVersion: String = "1.2",
  ): Call<QuestionnairePullResponse>
}
