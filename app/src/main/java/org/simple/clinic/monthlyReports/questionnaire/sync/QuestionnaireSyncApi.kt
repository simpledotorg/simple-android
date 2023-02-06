package org.simple.clinic.monthlyReports.questionnaire.sync

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface QuestionnaireSyncApi {

  @Headers(value = ["X-RESYNC-TOKEN: 1"])
  @GET("/test.simple")
  fun pull(
      //      @Query("limit") recordsToPull: Int,
      //      @Query("process_token") lastPullToken: String? = null,
      //      @Query("dsl_version") dslVersion: Int = 1,
  ): Call<QuestionnairePullResponse>
}
