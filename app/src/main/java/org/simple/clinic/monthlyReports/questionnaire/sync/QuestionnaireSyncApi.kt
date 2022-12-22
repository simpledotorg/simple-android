package org.simple.clinic.monthlyReports.questionnaire.sync

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface QuestionnaireSyncApi {

  @Headers(value = ["X-RESYNC-TOKEN: 1"])
  @GET("v4/questionnaires/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("questionnaire_dsl_version") questionnaireDslVersion: String? = null,
      @Query("process_token") lastPullToken: String? = null
  ): Call<QuestionnairePullResponse>
}
