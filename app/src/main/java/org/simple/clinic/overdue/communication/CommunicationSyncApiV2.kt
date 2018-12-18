package org.simple.clinic.overdue.communication

import io.reactivex.Single
import org.simple.clinic.sync.DataPushResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CommunicationSyncApiV2 {

  @POST("v2/communications/sync")
  fun push(
      @Body body: CommunicationPushRequest
  ): Single<DataPushResponse>

  @GET("v2/communications/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Single<CommunicationPullResponse>
}
