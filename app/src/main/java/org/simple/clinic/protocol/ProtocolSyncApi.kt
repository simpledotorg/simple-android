package org.simple.clinic.protocol

import io.reactivex.Single
import org.simple.clinic.CURRENT_API_VERSION
import retrofit2.http.GET
import retrofit2.http.Query

interface ProtocolSyncApi {

  @GET("$CURRENT_API_VERSION/protocols/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Single<ProtocolPullResponse>
}
