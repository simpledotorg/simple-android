package org.simple.clinic.questionnaireresponse.sync

import org.simple.clinic.sync.DataPushResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface QuestionnaireResponseSyncApi {

  @POST("v4/questionnaire_responses/sync")
  fun push(
      @Body body: QuestionnaireResponsePushRequest
  ): Call<DataPushResponse>

  @Headers(value = ["X-RESYNC-TOKEN: 1"])
  @GET("v4/questionnaire_responses/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null,
  ): Call<QuestionnaireResponsePullResponse>
}
